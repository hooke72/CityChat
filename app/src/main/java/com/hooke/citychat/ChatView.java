package com.hooke.citychat;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.icu.text.DateFormat;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference myRef;
    static Context appContext;
    Room room;

    FirebaseRecyclerAdapter<Chat, ChatView.ChatViewHolder> adapterChat;

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView nAuthor;
        TextView nTimeDate;
        TextView nChatText;
        TextView nCityRoom;
        CardView mRoomCardView;


        public ChatViewHolder(View itemView) {
            super(itemView);
            nAuthor = itemView.findViewById(R.id.author);
            nTimeDate = itemView.findViewById(R.id.time_date);
            nChatText = itemView.findViewById(R.id.chat_text);
            nCityRoom = itemView.findViewById(R.id.name_city_room);
            mRoomCardView = itemView.findViewById(R.id.room_card_view);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setTitle("");

        appContext = getApplicationContext();
        // making list of room name

        myRef = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_list_city);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, 1));

        room = new Room(getIntent().getExtras().getString("nameCity"), getIntent().getExtras().getLong("idCity"), getIntent().getExtras().getString("nameRoom"), getIntent().getExtras().getLong("idRoom"));
        adapterChat = new FirebaseRecyclerAdapter<Chat, ChatView.ChatViewHolder>(
                Chat.class,
                R.layout.list_chat_item,
                ChatView.ChatViewHolder.class,
                myRef.child("citychat/" + room.idRoom + "/").orderByChild("timestamp")
        ) {
            @Override
            protected void populateViewHolder(final ChatView.ChatViewHolder viewHolder, final Chat chat, final int position) {

                viewHolder.nAuthor.setText(chat.author);
                viewHolder.nTimeDate.setText("time"); //chat.timestamp);
                viewHolder.nChatText.setText(chat.text);
                viewHolder.nCityRoom.setText(room.nameCity + ":" + room.nameRoom);
                //int color = getTrueColor(position);
                //((CardView) viewHolder.itemView).setCardBackgroundColor(color);

                viewHolder.mRoomCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {




                    }
                });

            }
        };

        recyclerView.setAdapter(adapterChat);
        //RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        //recyclerView.setItemAnimator(itemAnimator);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static int getTrueColor(int catid) {
        int col;
        int lCatid = catid % 5;
        Resources arrayColors = appContext.getResources();
        switch (lCatid) {
            case 1:
                col = arrayColors.getColor(R.color.mesgCat1);
                break;
            case 2:
                col = arrayColors.getColor(R.color.mesgCat2);
                break;
            case 3:
                col = arrayColors.getColor(R.color.mesgCat3);
                break;
            case 4:
                col = arrayColors.getColor(R.color.mesgCat4);
                break;
            case 5:
                col = arrayColors.getColor(R.color.mesgCat5);
                break;
            case 99:
                col = arrayColors.getColor(R.color.mesgCat99);
                break;
            default:
                col = arrayColors.getColor(R.color.mesgCatDef);
        }

        return col;
    }
}
