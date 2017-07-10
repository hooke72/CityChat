package com.hooke.citychat;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Hooke on 07.07.2017.
 */

public class ChoiceRoom extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference myRef;
    static Context appContext;
    City city;

    FirebaseRecyclerAdapter<Room,ChoiceRoom.RoomViewHolder> adapterRoom;
    public static class RoomViewHolder extends RecyclerView.ViewHolder{

        TextView nNameCity;
        TextView nNameRoom;
        CardView mRoomCardView;


        public RoomViewHolder(View itemView) {
            super(itemView);
            nNameCity = itemView.findViewById(R.id.name_city);
            nNameRoom = itemView.findViewById(R.id.name_room);
            mRoomCardView = itemView.findViewById(R.id.room_card_view);

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
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

        setTitle( "Select Room");

        appContext = getApplicationContext();
        // making list of room name

        myRef = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_list_city);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1 ,1));

        city = new City (getIntent().getExtras().getString("nameCity"),getIntent().getExtras().getLong("idCity"));
        adapterRoom = new FirebaseRecyclerAdapter<Room, ChoiceRoom.RoomViewHolder>(
                Room.class,
                R.layout.list_room_item,
                ChoiceRoom.RoomViewHolder.class,
                myRef.child("/catalog/rooms/"+city.idCity+"/").orderByChild("nameRooms")
        ) {
            @Override
            protected void populateViewHolder(final ChoiceRoom.RoomViewHolder viewHolder, final Room room, final int position) {

                viewHolder.nNameCity.setText(city.nameCity);
                viewHolder.nNameRoom.setText(room.nameRoom);
                //int color = getTrueColor(position);
                //((CardView) viewHolder.itemView).setCardBackgroundColor(color);

                viewHolder.mRoomCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        Intent intent = new Intent(appContext, ChatView.class);
                        intent.putExtra("idRoomy", room.idRoom);
                        intent.putExtra("nameRoom", room.nameRoom);
                        intent.putExtra("idRoomy", room.idCity);
                        intent.putExtra("nameRoom", room.nameCity);


                        startActivity(intent);

                    }
                });

            }
        };

        recyclerView.setAdapter(adapterRoom);
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

}
