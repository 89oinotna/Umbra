package com.oinotna.umbra.ui.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.oinotna.umbra.R;
import com.oinotna.umbra.input.keyboard.Keyboard;
import com.oinotna.umbra.input.keyboard.Keys;

public class Keyboard_View extends LinearLayout implements View.OnTouchListener {
    // constructors
    public Keyboard_View(Context context) {
        this(context, null, 0);
    }

    public Keyboard_View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Keyboard_View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // keyboard keys (buttons)
    private Button btn_oem_5;
    private Button btn_1;
    private Button btn_2;
    private Button btn_3;
    private Button btn_4;
    private Button btn_5;
    private Button btn_6;
    private Button btn_7;
    private Button btn_8;
    private Button btn_9;
    private Button btn_0;
    private Button btn_oem_4;
    private Button btn_oem_6;
    private Button btn_back;
    private Button btn_tab;
    private Button btn_key_q;
    private Button btn_key_w;
    private Button btn_key_e;
    private Button btn_key_r;
    private Button btn_key_t;
    private Button btn_key_y;
    private Button btn_key_u;
    private Button btn_key_i;
    private Button btn_key_o;
    private Button btn_key_p;
    private Button btn_oem_1;
    private Button btn_oem_plus;
    private Button btn_capital;
    private Button btn_key_a;
    private Button btn_key_s;
    private Button btn_key_d;
    private Button btn_key_f;
    private Button btn_key_g;
    private Button btn_key_h;
    private Button btn_key_j;
    private Button btn_key_k;
    private Button btn_key_l;
    private Button btn_oem_3;
    private Button btn_oem_7;
    private Button btn_oem_2;
    private Button btn_return;
    private Button btn_lshift;
    private Button btn_oem_102;
    private Button btn_key_z;
    private Button btn_key_x;
    private Button btn_key_c;
    private Button btn_key_v;
    private Button btn_key_b;
    private Button btn_key_m;
    private Button btn_key_n;
    private Button btn_oem_comma;
    private Button btn_oem_period;
    private Button btn_oem_minus;
    private Button btn_rshift;
    private Button btn_lcontrol;
    private Button btn_lwin;
    private Button btn_lmenu;
    private Button btn_space;
    private Button btn_rmenu;
    private Button btn_boh;
    private Button btn_apps;
    private Button btn_rcontrol;

    // This will map the button resource id to the String value that we want to
    // input when that button is clicked.
    SparseIntArray keyValues = new SparseIntArray();

    private void init(Context context, AttributeSet attrs) {

        // initialize buttons
        LayoutInflater.from(context).inflate(R.layout.keyboard_view, this, true);

        btn_oem_5= (Button) findViewById(R.id.btn_oem_5);
        btn_1= (Button) findViewById(R.id.btn_1);
        btn_2= (Button) findViewById(R.id.btn_2);
        btn_3= (Button) findViewById(R.id.btn_3);
        btn_4= (Button) findViewById(R.id.btn_4);
        btn_5= (Button) findViewById(R.id.btn_5);
        btn_6= (Button) findViewById(R.id.btn_6);
        btn_7= (Button) findViewById(R.id.btn_7);
        btn_8= (Button) findViewById(R.id.btn_8);
        btn_9= (Button) findViewById(R.id.btn_9);
        btn_0= (Button) findViewById(R.id.btn_0);
        btn_oem_4= (Button) findViewById(R.id.btn_oem_4);
        btn_oem_6= (Button) findViewById(R.id.btn_oem_6);
        btn_back= (Button) findViewById(R.id.btn_back);
        btn_tab= (Button) findViewById(R.id.btn_tab);
        btn_key_q= (Button) findViewById(R.id.btn_key_q);
        btn_key_w= (Button) findViewById(R.id.btn_key_w);
        btn_key_e= (Button) findViewById(R.id.btn_key_e);
        btn_key_r= (Button) findViewById(R.id.btn_key_r);
        btn_key_t= (Button) findViewById(R.id.btn_key_t);
        btn_key_y= (Button) findViewById(R.id.btn_key_y);
        btn_key_u= (Button) findViewById(R.id.btn_key_u);
        btn_key_i= (Button) findViewById(R.id.btn_key_i);
        btn_key_o= (Button) findViewById(R.id.btn_key_o);
        btn_key_p= (Button) findViewById(R.id.btn_key_p);
        btn_oem_1= (Button) findViewById(R.id.btn_oem_1);
        btn_oem_plus= (Button) findViewById(R.id.btn_oem_plus);
        btn_capital= (Button) findViewById(R.id.btn_capital);
        btn_key_a= (Button) findViewById(R.id.btn_key_a);
        btn_key_s= (Button) findViewById(R.id.btn_key_s);
        btn_key_d= (Button) findViewById(R.id.btn_key_d);
        btn_key_f= (Button) findViewById(R.id.btn_key_f);
        btn_key_g= (Button) findViewById(R.id.btn_key_g);
        btn_key_h= (Button) findViewById(R.id.btn_key_h);
        btn_key_j= (Button) findViewById(R.id.btn_key_j);
        btn_key_k= (Button) findViewById(R.id.btn_key_k);
        btn_key_l= (Button) findViewById(R.id.btn_key_l);
        btn_oem_3= (Button) findViewById(R.id.btn_oem_3);
        btn_oem_7= (Button) findViewById(R.id.btn_oem_7);
        btn_oem_2= (Button) findViewById(R.id.btn_oem_2);
        btn_return= (Button) findViewById(R.id.btn_return);
        btn_lshift= (Button) findViewById(R.id.btn_lshift);
        btn_oem_102= (Button) findViewById(R.id.btn_oem_102);
        btn_key_z= (Button) findViewById(R.id.btn_key_z);
        btn_key_x= (Button) findViewById(R.id.btn_key_x);
        btn_key_c= (Button) findViewById(R.id.btn_key_c);
        btn_key_v= (Button) findViewById(R.id.btn_key_v);
        btn_key_b= (Button) findViewById(R.id.btn_key_b);
        btn_key_m= (Button) findViewById(R.id.btn_key_m);
        btn_key_n= (Button) findViewById(R.id.btn_key_n);
        btn_oem_comma= (Button) findViewById(R.id.btn_oem_comma);
        btn_oem_period= (Button) findViewById(R.id.btn_oem_period);
        btn_oem_minus= (Button) findViewById(R.id.btn_oem_minus);
        btn_rshift= (Button) findViewById(R.id.btn_rshift);
        btn_lcontrol= (Button) findViewById(R.id.btn_lcontrol);
        btn_lwin= (Button) findViewById(R.id.btn_lwin);
        btn_lmenu= (Button) findViewById(R.id.btn_lmenu);
        btn_space= (Button) findViewById(R.id.btn_space);
        btn_rmenu= (Button) findViewById(R.id.btn_rmenu);
        btn_boh= (Button) findViewById(R.id.btn_boh);
        btn_apps= (Button) findViewById(R.id.btn_apps);
        btn_rcontrol= (Button) findViewById(R.id.btn_rcontrol);

        // set button click listeners
        btn_oem_5.setOnTouchListener(this);
        btn_1.setOnTouchListener(this);
        btn_2.setOnTouchListener(this);
        btn_3.setOnTouchListener(this);
        btn_4.setOnTouchListener(this);
        btn_5.setOnTouchListener(this);
        btn_6.setOnTouchListener(this);
        btn_7.setOnTouchListener(this);
        btn_8.setOnTouchListener(this);
        btn_9.setOnTouchListener(this);
        btn_0.setOnTouchListener(this);
        btn_oem_4.setOnTouchListener(this);
        btn_oem_6.setOnTouchListener(this);
        btn_back.setOnTouchListener(this);
        btn_tab.setOnTouchListener(this);
        btn_key_q.setOnTouchListener(this);
        btn_key_w.setOnTouchListener(this);
        btn_key_e.setOnTouchListener(this);
        btn_key_r.setOnTouchListener(this);
        btn_key_t.setOnTouchListener(this);
        btn_key_y.setOnTouchListener(this);
        btn_key_u.setOnTouchListener(this);
        btn_key_i.setOnTouchListener(this);
        btn_key_o.setOnTouchListener(this);
        btn_key_p.setOnTouchListener(this);
        btn_oem_1.setOnTouchListener(this);
        btn_oem_plus.setOnTouchListener(this);
        btn_capital.setOnTouchListener(this);
        btn_key_a.setOnTouchListener(this);
        btn_key_s.setOnTouchListener(this);
        btn_key_d.setOnTouchListener(this);
        btn_key_f.setOnTouchListener(this);
        btn_key_g.setOnTouchListener(this);
        btn_key_h.setOnTouchListener(this);
        btn_key_j.setOnTouchListener(this);
        btn_key_k.setOnTouchListener(this);
        btn_key_l.setOnTouchListener(this);
        btn_oem_3.setOnTouchListener(this);
        btn_oem_7.setOnTouchListener(this);
        btn_oem_2.setOnTouchListener(this);
        btn_return.setOnTouchListener(this);
        btn_lshift.setOnTouchListener(this);
        btn_oem_102.setOnTouchListener(this);
        btn_key_z.setOnTouchListener(this);
        btn_key_x.setOnTouchListener(this);
        btn_key_c.setOnTouchListener(this);
        btn_key_v.setOnTouchListener(this);
        btn_key_b.setOnTouchListener(this);
        btn_key_m.setOnTouchListener(this);
        btn_key_n.setOnTouchListener(this);
        btn_oem_comma.setOnTouchListener(this);
        btn_oem_period.setOnTouchListener(this);
        btn_oem_minus.setOnTouchListener(this);
        btn_rshift.setOnTouchListener(this);
        btn_lcontrol.setOnTouchListener(this);
        btn_lwin.setOnTouchListener(this);
        btn_lmenu.setOnTouchListener(this);
        btn_space.setOnTouchListener(this);
        btn_rmenu.setOnTouchListener(this);
        btn_boh.setOnTouchListener(this);
        btn_apps.setOnTouchListener(this);
        btn_rcontrol.setOnTouchListener(this);

        // map buttons IDs to input strings
        keyValues.put(R.id.btn_oem_5, Keys.OEM_5);
        keyValues.put(R.id.btn_1, Keys.KEY_1 );
        keyValues.put(R.id.btn_2, Keys.KEY_2 );
        keyValues.put(R.id.btn_3, Keys.KEY_3 );
        keyValues.put(R.id.btn_4, Keys.KEY_4 );
        keyValues.put(R.id.btn_5, Keys.KEY_5 );
        keyValues.put(R.id.btn_6, Keys.KEY_6 );
        keyValues.put(R.id.btn_7, Keys.KEY_7 );
        keyValues.put(R.id.btn_8, Keys.KEY_8 );
        keyValues.put(R.id.btn_9, Keys.KEY_9 );
        keyValues.put(R.id.btn_0, Keys.KEY_0 );
        keyValues.put(R.id.btn_oem_4, Keys.OEM_4 );
        keyValues.put(R.id.btn_oem_6, Keys.OEM_6 );
        keyValues.put(R.id.btn_back, Keys.BACK );
        keyValues.put(R.id.btn_tab, Keys.TAB );
        keyValues.put(R.id.btn_key_q, Keys.KEY_Q );
        keyValues.put(R.id.btn_key_w, Keys.KEY_W );
        keyValues.put(R.id.btn_key_e, Keys.KEY_E );
        keyValues.put(R.id.btn_key_r, Keys.KEY_R );
        keyValues.put(R.id.btn_key_t, Keys.KEY_T );
        keyValues.put(R.id.btn_key_y, Keys.KEY_Y );
        keyValues.put(R.id.btn_key_u, Keys.KEY_U );
        keyValues.put(R.id.btn_key_i, Keys.KEY_I );
        keyValues.put(R.id.btn_key_o, Keys.KEY_O );
        keyValues.put(R.id.btn_key_p, Keys.KEY_P );
        keyValues.put(R.id.btn_oem_1, Keys.OEM_1 );
        keyValues.put(R.id.btn_oem_plus, Keys.OEM_PLUS );
        keyValues.put(R.id.btn_capital, Keys.CAPITAL );
        keyValues.put(R.id.btn_key_a, Keys.KEY_A );
        keyValues.put(R.id.btn_key_s, Keys.KEY_S );
        keyValues.put(R.id.btn_key_d, Keys.KEY_D );
        keyValues.put(R.id.btn_key_f, Keys.KEY_F );
        keyValues.put(R.id.btn_key_g, Keys.KEY_G );
        keyValues.put(R.id.btn_key_h, Keys.KEY_H );
        keyValues.put(R.id.btn_key_j, Keys.KEY_J );
        keyValues.put(R.id.btn_key_k, Keys.KEY_K );
        keyValues.put(R.id.btn_key_l, Keys.KEY_L );
        keyValues.put(R.id.btn_oem_3, Keys.OEM_3 );
        keyValues.put(R.id.btn_oem_7, Keys.OEM_7 );
        keyValues.put(R.id.btn_oem_2, Keys.OEM_2 );
        keyValues.put(R.id.btn_return, Keys.RETURN );
        keyValues.put(R.id.btn_lshift, Keys.LSHIFT );
        keyValues.put(R.id.btn_oem_102, Keys.OEM_102 );
        keyValues.put(R.id.btn_key_z, Keys.KEY_Z );
        keyValues.put(R.id.btn_key_x, Keys.KEY_X );
        keyValues.put(R.id.btn_key_c, Keys.KEY_C );
        keyValues.put(R.id.btn_key_v, Keys.KEY_V );
        keyValues.put(R.id.btn_key_b, Keys.KEY_B );
        keyValues.put(R.id.btn_key_m, Keys.KEY_M );
        keyValues.put(R.id.btn_key_n, Keys.KEY_N );
        keyValues.put(R.id.btn_oem_comma, Keys.OEM_COMMA );
        keyValues.put(R.id.btn_oem_period, Keys.OEM_PERIOD );
        keyValues.put(R.id.btn_oem_minus, Keys.OEM_MINUS );
        keyValues.put(R.id.btn_rshift, Keys.RSHIFT );
        keyValues.put(R.id.btn_lcontrol, Keys.LCONTROL );
        keyValues.put(R.id.btn_lwin, Keys.LWIN );
        keyValues.put(R.id.btn_lmenu, Keys.LMENU );
        keyValues.put(R.id.btn_space, Keys.SPACE );
        keyValues.put(R.id.btn_rmenu, Keys.RMENU );
        keyValues.put(R.id.btn_boh, Keys.NONAME );
        keyValues.put(R.id.btn_apps, Keys.APPS );
        keyValues.put(R.id.btn_rcontrol, Keys.RCONTROL );
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
         switch (event.getAction()){
             case MotionEvent.ACTION_DOWN:
                 Keyboard.sendKeyDown(keyValues.get(v.getId()));
             case MotionEvent.ACTION_UP:
                 Keyboard.sendKeyUp(keyValues.get(v.getId()));
         }
         return  true;
    }
}
