package com.oinotna.umbra.ui.home;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.oinotna.umbra.R;
import com.oinotna.umbra.SecretKeyViewModel;
import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.input.MySocket;
import com.oinotna.umbra.input.MySocketViewModel;
import com.oinotna.umbra.thread.Finder;
import com.oinotna.umbra.ui.mouse.MouseViewModel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;

public class HomeFragment extends Fragment implements  View.OnClickListener, ServersAdapter.onItemClickListner, Observer<Byte>, ServersAdapter.onMenuItemClickListener, ProgressButton.OnAnimationEndListener {

    private HomeViewModel homeViewModel;
    private MySocketViewModel mySocketViewModel;
    private ServersAdapter mAdapter;
    private RecyclerView rv;
    private RecyclerView.LayoutManager lm;
    private ArrayList<ServerPc> serversList;


    private MouseViewModel mouseViewModel;
    private PasswordDialogViewModel passwordDialogViewModel;
    private SecretKeyViewModel secretKeyViewModel;

    private ProgressButton btnSearch;


    //TODO trigger dialog on longpress cardview to change password
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnSearch = root.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(this);
        btnSearch.setOnAnimationEndListener(this);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        mySocketViewModel=new ViewModelProvider(requireActivity()).get(MySocketViewModel.class);

        serversList = homeViewModel.getServersList();

        lm=new GridLayoutManager(getContext(), 3);
        rv=root.findViewById(R.id.rv_servers);
        rv.setLayoutManager(lm);
        mAdapter=new ServersAdapter(serversList);
        mAdapter.setOnItemClickListner(this);
        mAdapter.setOnMenuItemClickListener(this);
        rv.setAdapter(mAdapter);
        rv.setNestedScrollingEnabled(true);

        WifiManager wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager==null || !wifiManager.isWifiEnabled()) {
            //se rimane qualcosa la tolgo
            serversList.clear();
            mAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), R.string.connect_wifi, Toast.LENGTH_SHORT).show();
        }

        //mi permette di aggiornare la recycler view
        //uso serverpc solo per triggerare notifyDataSetChanged
        homeViewModel.getLiveServersList().observe(getViewLifecycleOwner(), serverPcs -> mAdapter.notifyDataSetChanged());

        mouseViewModel =new ViewModelProvider(requireActivity()).get(MouseViewModel.class);

        secretKeyViewModel=new ViewModelProvider(requireActivity()).get(SecretKeyViewModel.class);

        return root;
    }


    /**
     * onClick for the search button
     * Check for wifi enabled, start button animation, start server search
     */
    @Override
    public void onClick(View v) {
        if(v.getId()==(R.id.btn_search)){
            WifiManager wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager!=null && wifiManager.isWifiEnabled()) {
                btnSearch.startAnimation();
                try {
                    InetAddress broadcast=getBroadcastAddress();
                    if(broadcast!=null)
                        homeViewModel.searchForServers(broadcast);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
            else{
                serversList.clear();
                mAdapter.notifyDataSetChanged(); //se rimane qualcosa la tolgo
                Toast.makeText(getContext(), R.string.connect_wifi, Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * When button animation end restore previous state
     * Make toast if nothing found
     */
    @Override
    public void onAnimationEnd() {
        btnSearch.startEndAnimation(); //per farla a contrario
        if(serversList.isEmpty()){
            Toast.makeText(requireActivity(), R.string.toast_nothing_found, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * onItemClick for CardView inside the RecyclerView
     * End server search and connect to selected server
     * @param position position of the selected server
     */
    @Override
    public void onItemClick(int position) {
        ServerPc pc = serversList.get(position);
        Log.d("LOG", "connessione a: " + pc.getFullName());
        Finder.endSearch();
        MySocket.connect(new ServerPc(pc.getName(), pc.getIp())).observe(getViewLifecycleOwner(), this);
    }

    /**
     * Click on the ContextMenu of a CardView
     * @param itemId id of the selected menu item
     * @param position position of the server in the list
     */
    @Override
    public boolean onMenuItemClick(int itemId, int position) {
        if(itemId==1){
            requirePassword(serversList.get(position));
            return true; //todo ok???
        }
        return false;
    }

    /**
     * Retrive the broadcast address of the connected network
     * @return Broadcast address
     * @throws UnknownHostException
     */
    private InetAddress getBroadcastAddress() throws UnknownHostException {
        WifiManager wifi = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifi==null)
            return null;
        DhcpInfo dhcp = wifi.getDhcpInfo();
        int broadcast = dhcp.ipAddress  | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));
        return InetAddress.getByAddress(quads);
    }

    /**
     * Observer for connection status
     * @param o
     */
    @Override
    public void onChanged(Byte o) {
        if(o == MySocket.CONNECTED || o == MySocket.CONNECTED_PASSWORD) {
            mySocketViewModel.setPc(MySocket.getInstance().getPc());
            ((BottomNavigationView) requireActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.mouse);
        }
        else if(o == MySocket.REQUIRE_PASSWORD){
            LiveData<ServerPc> pc=homeViewModel.getPC(MySocket.getInstance().getPc().getName());
            subscribeToServerPc(pc); //controllo se è presente nel db
        }
        else if(o == MySocket.WRONG_PASSWORD){
            Toast.makeText(requireActivity(), R.string.toast_wrong_password, Toast.LENGTH_SHORT).show();
            requirePassword(MySocket.getInstance().getPc());
        }
        else if(o == MySocket.CONNECTION_ERROR) { //CONNECTION_ERROR
            mySocketViewModel.setPc(null);
            Toast.makeText(requireActivity(), R.string.toast_cant_connect, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Subscribe observer for LiveData<ServerPc>
     * Used when waiting for a query
     * Requires password for the server if the query return null
     * One shot observer (autoremove)
     */
    private void subscribeToServerPc(LiveData<ServerPc> pc){
        //no lambda altrimenti non posso usare this!!
        pc.observe(getViewLifecycleOwner(), new Observer<ServerPc>() {
            @Override
            public void onChanged(ServerPc serverPc) {
                //rimuovo l'observer
                pc.removeObserver(this);
                //serverpc has the base64 of the aes encrypted password
                if(serverPc!=null){
                    String encryptedBase64=serverPc.getPassword();
                    byte[] decoded;
                    // get base64 encoded version of the key
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        decoded = Base64.getDecoder().decode(encryptedBase64);
                    }
                    else{
                        decoded = android.util.Base64.decode(encryptedBase64, android.util.Base64.DEFAULT);
                    }
                    MySocket.getInstance().usePassword(secretKeyViewModel.decrypt(decoded));

                }
                //se non è presente devo chiedere la password
                else {
                    //ServerPc pc=mouseViewModel.getPc();
                    requirePassword(MySocket.getInstance().getPc());
                }
            }
        });
    }

    /**
     * Requires password for a server
     * Shows a PasswordDialog and retrieve password from it
     * Stores the password
     * @param pc
     */
    private void requirePassword(ServerPc pc){
        passwordDialogViewModel =new ViewModelProvider(requireActivity()).get(PasswordDialogViewModel.class);
        passwordDialogViewModel.getPassword().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                passwordDialogViewModel.getPassword().removeObserver(this);
                ServerPc store=new ServerPc(pc.getName(), pc.getIp());
                store.setPassword(secretKeyViewModel.encrypt(s.getBytes()));
                homeViewModel.storePc(store); //salvo nel db
                pc.setPassword(s);
                if(mySocketViewModel.getConnection()!=null
                        && mySocketViewModel.getConnection().getValue()!=null
                        && (mySocketViewModel.getConnection().getValue()==MySocket.REQUIRE_PASSWORD || mySocketViewModel.getConnection().getValue()==MySocket.WRONG_PASSWORD))
                    MySocket.getInstance().usePassword(s); //riprovo la connessione con la password
            }
        });
        DialogFragment df = new PasswordDialog();
        df.show(requireActivity().getSupportFragmentManager(), "password");
    }

}