package com.hooke.citychat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hooke.citychat.model.Room;

/**
 * Activity showing list of user's chat and will provide to adding some new chat
 */


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String PUTEXTRA_UID = "uid";
    public static final String PATH_USERS_ROOMS = "/usersRooms/";
    public static final String PATH_CATALOG_ROOMS = "/catalog/rooms/";
    public static final String PATH_CATALOG = "/catalog/";
    public static final String PATH_SETTINGS = "/settings/";
    public static final String PATH_PAYLOAD = "/payload";
    public static final String PUTEXTRA_IDROOM = "idRoom";
    public static final String PUTEXTRA_NAMEROOM = "nameRoom";
    public static final String PUTEXTRA_IDCITY = "idCity";
    public static final String PUTEXTRA_NAMECITY = "nameCity";
    public static final String MARKER_NOAUTH = "noauth";
    public static final String TEXT_PRE_CITY = "       ";
    public static final String TEXT_PRE_ROOM = " ";
    public static String CONTENT_URI = "com.hooke.citychat";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private String uid = MARKER_NOAUTH;
    private boolean succesLoged = false;
    private boolean firstPleaseLogin = true;

    private ShareActionProvider mShareActionProvider;
    private StorageReference mStorageReference;

    ImageView imageUser;
    TextView loginUser;
    TextView nameUser;

    static Context appContext;

    FirebaseRecyclerAdapter<Room, RoomViewHolder> adapterRoom;

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView mNameCity;
        TextView nNameRoom;
        CardView mRoomCardView;
        ImageView mNewPost;
        ImageView cityIcon;

        public RoomViewHolder(View itemView) {
            super(itemView);
            mNameCity = (TextView) itemView.findViewById(R.id.name_city);
            nNameRoom = (TextView) itemView.findViewById(R.id.name_room);
            mNewPost = (ImageView) itemView.findViewById(R.id.new_post);
            mRoomCardView = (CardView) itemView.findViewById(R.id.room_card_view);
            cityIcon = (ImageView) itemView.findViewById(R.id.city_icon);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_recycler_view);

        DatabaseUtil.setPersistenceEnabled();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (succesLoged) {
                    Intent intent = new Intent(appContext, SelectChat.class);
                    intent.putExtra(PUTEXTRA_UID, uid);
                    startActivity(intent);
                } else {
                    Snackbar.make(view, R.string.please_login, Snackbar.LENGTH_LONG);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        appContext = getApplicationContext();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        setTitle(R.string.main_activity);
        startAuthFirebase();

    }

    private void startFirebaseRecycleAdapter() {

        DatabaseReference usersRooms = FirebaseDatabase.getInstance().getReference(PATH_USERS_ROOMS + uid);
        usersRooms.keepSynced(true);
        DatabaseReference catalog = FirebaseDatabase.getInstance().getReference(PATH_CATALOG);
        catalog.keepSynced(true);
        DatabaseReference settings = FirebaseDatabase.getInstance().getReference(PATH_SETTINGS);
        settings.keepSynced(true);

        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_list);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, 1));

        adapterRoom = new FirebaseRecyclerAdapter<Room, MainActivity.RoomViewHolder>(
                Room.class,
                R.layout.items_list_room,
                MainActivity.RoomViewHolder.class,
                myRef.child(PATH_USERS_ROOMS + uid + "/").orderByChild(PUTEXTRA_IDROOM)
        ) {
            @Override
            protected void populateViewHolder(final MainActivity.RoomViewHolder viewHolder, final Room room, final int position) {
                markIfHaveNewPost(myRef, room, viewHolder);
                Glide.with(appContext)
                        .using(new FirebaseImageLoader())
                        .load(mStorageReference.child(room.iconPath))
                        .into(viewHolder.cityIcon);
                viewHolder.mNameCity.setText(TEXT_PRE_CITY + room.nameCity);
                viewHolder.nNameRoom.setText(TEXT_PRE_ROOM + room.nameRoom);
                viewHolder.mRoomCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(appContext, ChatView.class);
                        intent.putExtra(PUTEXTRA_IDROOM, room.idRoom);
                        intent.putExtra(PUTEXTRA_NAMEROOM, room.nameRoom);
                        intent.putExtra(PUTEXTRA_IDCITY, room.idCity);
                        intent.putExtra(PUTEXTRA_NAMECITY, room.nameCity);
                        intent.putExtra(PUTEXTRA_UID, uid);
                        startActivity(intent);
                    }
                });

            }
        };
        recyclerView.setAdapter(adapterRoom);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.swipe_unsubscribe)
                        + " " + adapterRoom.getItem(viewHolder.getAdapterPosition()).nameRoom, Toast.LENGTH_LONG).show();
                FirebaseDatabase.getInstance().getReference().child(PATH_USERS_ROOMS + uid + "/"
                        + adapterRoom.getItem(viewHolder.getAdapterPosition()).idRoom).removeValue();
                adapterRoom.notifyDataSetChanged();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void markIfHaveNewPost(DatabaseReference myRef, final Room room, final RoomViewHolder viewHolder) {
        myRef.child(PATH_CATALOG_ROOMS + "/" + room.idCity + "/" + room.idRoom + PATH_PAYLOAD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long lastPostTime = dataSnapshot.getValue(long.class);
                if (lastPostTime > room.timeLastRead) {
                    viewHolder.mNewPost.setImageDrawable(getResources().getDrawable(R.drawable.new_post));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, R.string.database_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillNavHeader() {
        imageUser = (ImageView) findViewById(R.id.imageUser);
        loginUser = (TextView) findViewById(R.id.loginUser);
        nameUser = (TextView) findViewById(R.id.nameUser);
        if (user != null) {
            loginUser.setText(user.getEmail());
            nameUser.setText(user.getDisplayName());
            Glide.with(this).load(user.getPhotoUrl().toString()).into(imageUser);
        } else if (loginUser != null) {
            loginUser.setText(R.string.no_login);
            nameUser.setText(R.string.no_user);
        }
    }

    private void startAuthFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                    succesLoged = true;
                } else {
                    uid = MARKER_NOAUTH;
                    if (firstPleaseLogin) {
                        firstPleaseLogin = false;
                        loginDialog();
                    }
                }
                startFirebaseRecycleAdapter();
            }
        };
    }

    private void loginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.please_login)
                .setCancelable(false)
                .setPositiveButton(R.string.please_login_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), GoogleLoginActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.please_login_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void goAuth(View v) {
        startActivity(new Intent(MainActivity.this, GoogleLoginActivity.class));
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


        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        doShare();
        fillNavHeader();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_item_share:
                break;
            default:
                break;
        }

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    private void doShare() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_app));
        mShareActionProvider.setShareIntent(sendIntent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            Intent intent = new Intent(appContext, AboutPageActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        fillNavHeader();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
