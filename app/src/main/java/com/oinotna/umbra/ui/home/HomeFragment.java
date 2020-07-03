package com.oinotna.umbra.ui.home;

import android.app.Notification;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
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
import com.oinotna.umbra.mouse.MouseSocket;
import com.oinotna.umbra.ui.mouse.MouseViewModel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener, ServersAdapter.onItemClickListner, Observer<Byte>, ServersAdapter.onMenuItemClickListener{

    private HomeViewModel homeViewModel;
    private ServersAdapter mAdapter;
    private RecyclerView rv;
    private RecyclerView.LayoutManager lm;
    private ArrayList<ServerPc> serversList;

    private MouseViewModel mouseViewModel;
    private DialogPasswordViewModel dialogPasswordViewModel;
    private SecretKeyViewModel secretKeyViewModel;


    //TODO trigger dialog on longpress cardview to change password
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        Button btnSearch = root.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(this);

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        serversList = homeViewModel.getServersList();

        lm=new GridLayoutManager(getContext(), 3);
        rv=root.findViewById(R.id.rv_servers);
        rv.setLayoutManager(lm);
        mAdapter=new ServersAdapter(serversList);
        mAdapter.setOnItemClickListner(this);
        mAdapter.setOnMenuItemClickListener(this);
        rv.setAdapter(mAdapter);
        rv.setNestedScrollingEnabled(true);
        //registerForContextMenu(rv);

        //mi permette di aggiornare la recycler view
        //uso serverpc solo per triggerare notifyDataSetChanged
        homeViewModel.getLiveServersList().observe(getViewLifecycleOwner(), serverPcs -> mAdapter.notifyDataSetChanged());

        mouseViewModel =new ViewModelProvider(requireActivity()).get(MouseViewModel.class);
        //registro l'observer sulla connessione
        mouseViewModel.getConnection().observe(getViewLifecycleOwner(), this);

        secretKeyViewModel=new ViewModelProvider(requireActivity()).get(SecretKeyViewModel.class);

        return root;
    }


    /**
     * onClick per search
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(v.getId()==(R.id.btn_search)){
            WifiManager wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager!=null && wifiManager.isWifiEnabled()) {
                try {
                    InetAddress broadcast=getBroadcastAddress();
                    if(broadcast!=null)
                        homeViewModel.searchForServers(broadcast);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(getContext(), "Connect to WIFI", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * onClick per le cardview
     * @param position
     */
    @Override
    public void onClick(int position) {
        ServerPc pc = serversList.get(position);
        Log.d("LOG", "connessione a: " + pc.getFullName());
        homeViewModel.endSearch();
        try {
            mouseViewModel.connect(pc);
        }catch (SocketException e){
            Toast.makeText(requireActivity(), R.string.toast_cant_connect, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Restituisce l'indirizzo broadcast della rete
     * @return
     * @throws IOException
     */
    private InetAddress getBroadcastAddress() throws IOException {
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
     * observer for connection
     * @param o
     */
    @Override
    public void onChanged(Byte o) {
        if(o == MouseSocket.CONNECTED || o == MouseSocket.CONNECTED_PASSWORD) {
            ((BottomNavigationView) requireActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.mouse);
            //mouseViewModel.getConnection().removeObserver(this);
        }
        else if(o == MouseSocket.REQUIRE_PASSWORD){
            LiveData<ServerPc> pc=homeViewModel.getPC(mouseViewModel.getPc().getName());
            subscribeToServerPc(pc); //controllo se è presente nel db
        }
        else if(o == MouseSocket.WRONG_PASSWORD){
            Toast.makeText(requireActivity(), R.string.toast_wrong_password, Toast.LENGTH_SHORT).show();
            //todo show dialogfragment for password
        }
        else if(o == MouseSocket.CONNECTION_ERROR) { //CONNECTION_ERROR
            Toast.makeText(requireActivity(), R.string.toast_cant_connect, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Subscribe observer to a LiveData<ServerPc>
     * Used when waiting for a query
     * Se ho bisogno della password visualizza PasswordDialog
     * One shot observer (autoremove)
     * @param pc
     */
    private void subscribeToServerPc(LiveData<ServerPc> pc){
        //no lambda altrimenti non posso usare this!!
        pc.observe(getViewLifecycleOwner(), new Observer<ServerPc>() {
            @Override
            public void onChanged(ServerPc serverPc) {
                //serverpc has aes encrypted base64 password
                if(serverPc!=null){
                    mouseViewModel.usePassword(serverPc.getPassword());
                }
                //se non è presente devo chiedere la password
                else {

                    dialogPasswordViewModel=new ViewModelProvider(requireActivity()).get(DialogPasswordViewModel.class);
                    dialogPasswordViewModel.getPassword().observe(getViewLifecycleOwner(), new Observer<String>() {
                        @Override
                        public void onChanged(String s) {
                            ServerPc pc=mouseViewModel.getPc();
                            pc.setPassword(secretKeyViewModel.encrypt(s.getBytes()));
                            homeViewModel.storePc(pc); //salvo nel db
                            mouseViewModel.usePassword(s); //riprovo la connessione con la password
                            dialogPasswordViewModel.getPassword().removeObserver(this);
                        }
                    });
                    DialogFragment df = new PasswordDialog();
                    df.show(requireActivity().getSupportFragmentManager(), "password");

                }
                //rimuovo l'observer
                pc.removeObserver(this);
            }
        });
    }

    @Override
    public boolean onMenuItemClick(int itemId, int position) {
        if(itemId==1){
            dialogPasswordViewModel=new ViewModelProvider(requireActivity()).get(DialogPasswordViewModel.class);
            dialogPasswordViewModel.getPassword().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    ServerPc pc = serversList.get(position);
                    pc.setPassword(s);
                    homeViewModel.storePc(pc);
                    dialogPasswordViewModel.getPassword().removeObserver(this);
                    onClick(position); //todo non è tanto bello
                }
            });
            DialogFragment df = new PasswordDialog();
            df.show(requireActivity().getSupportFragmentManager(), "password");

        }
        return false;
    }


}