package com.hooke.citychat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputType;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.location.LocationServices;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hooke.citychat.model.CatalogItem;
import com.hooke.citychat.model.NextCity;
import com.hooke.citychat.model.NextRoom;
import com.hooke.citychat.model.Room;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity showing the list of cities or chats to add to the user
 * also may add new city where user are
 * or make new room
 */
public class SelectChat extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String UID = "uid";
    public static final String PATH_CATALOG_CITY = "/catalog/city/";
    public static final String PATH_CATALOG_ROOMS = "/catalog/rooms/";
    public static final String PATH_USERS_ROOMS = "/usersRooms/";
    public static final String PUTEXTRA_UID = "uid";
    public static final String PUTEXTRA_IDROOM = "idRoom";
    public static final String PUTEXTRA_NAMEROOM = "nameRoom";
    public static final String PUTEXTRA_IDCITY = "idCity";
    public static final String PUTEXTRA_NAMECITY = "nameCity";
    public static final String PATH_SETTINGS_NEXTIDROOM = "/settings/nextIdRoom/";
    public static final String PATH_SETTINGS_NEXTIDCITY = "/settings/nextIdCity/";
    public static final String CHILD_NAME = "name";
    public static final String CHILD_ID = "id";
    public static final String TEXT_PRE_NAME = "   ";
    public static final String TEXT_PRE_PAYLOAD = "   ";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String NO_ICON = "cityIcon/noicon.png";
    SimpleDateFormat dfDateTime = new SimpleDateFormat(DATE_TIME_FORMAT);
    private String uid;
    private DatabaseReference myRef;
    private boolean isCityChoiced = false;
    private CatalogItem choicedCity = new CatalogItem();
    private CatalogItem choicedRoom = new CatalogItem();
    RecyclerView recyclerView;
    private boolean isPermisionGranted = false;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    double lat;
    double lon;
    MaterialSearchView searchView;
    CharSequence searchString;
    private StorageReference mStorageReference;

    static Context appContext;

    FirebaseRecyclerAdapter<CatalogItem, SelectChat.SelectViewHolder> adapterSelect;

    public static class SelectViewHolder extends RecyclerView.ViewHolder {
        TextView nSelect;
        TextView nPayLoad;
        CardView mRoomCardView;
        ImageView cityIcon;

        public SelectViewHolder(View itemView) {
            super(itemView);
            nSelect = (TextView) itemView.findViewById(R.id.select);
            nPayLoad = (TextView) itemView.findViewById(R.id.payload);
            mRoomCardView = (CardView) itemView.findViewById(R.id.cv_select_chat);
            cityIcon = (ImageView) itemView.findViewById(R.id.city_icon);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_recycler_view);

        appContext = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCityChoiced) {
                    addRoomDialog();
                } else {
                    if (isPermisionGranted) {
                        setupLocationServices();
                    } else {
                        permissionGrantDialog();
                    }
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

        uid = getIntent().getExtras().getString(UID);
        myRef = FirebaseDatabase.getInstance().getReference();
        recyclerView = (RecyclerView) findViewById(R.id.rv_list);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.selectchat_background));
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        startFirebaseRecycleAdapterChoiceItem(PATH_CATALOG_CITY);
        setupSearchListener();
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    private void setupSearchListener() {
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(true);
        searchString = "";
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchString = query;
                for (int i = 0; i < adapterSelect.getItemCount(); i++) {
                    if (adapterSelect.getItem(i).name.contains(searchString)) {
                        recyclerView.smoothScrollToPosition(i);
                        break;
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
            }
        });
    }

    private void startFirebaseRecycleAdapterChoiceItem(String path) {

        if (isCityChoiced) {
            setTitle(getString(R.string.choice_room) + choicedCity.name);
        } else {
            setTitle(R.string.choice_city);
        }
        adapterSelect = new FirebaseRecyclerAdapter<CatalogItem, SelectViewHolder>(
                CatalogItem.class,
                R.layout.items_select_chat,
                SelectViewHolder.class,
                myRef.child(path).orderByChild(CHILD_NAME)
        ) {
            @Override
            protected void populateViewHolder(final SelectViewHolder viewHolder, final CatalogItem item, final int position) {
                viewHolder.nSelect.setText(TEXT_PRE_NAME + item.name);
                Glide.with(appContext)
                        .using(new FirebaseImageLoader())
                        .load(mStorageReference.child(item.iconPath))
                        .into(viewHolder.cityIcon);
                if (isCityChoiced) {
                    viewHolder.nPayLoad.setText(TEXT_PRE_PAYLOAD + getResources().getString(R.string.last_post_at) + dfDateTime.format(item.payload));
                } else {
                    viewHolder.nPayLoad.setText(TEXT_PRE_PAYLOAD + getResources().getString(R.string.number_of_chats) + item.payload);
                }
                viewHolder.mRoomCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isCityChoiced) {
                            isCityChoiced = true;
                            choicedCity.store(item);
                            startFirebaseRecycleAdapterChoiceItem(PATH_CATALOG_ROOMS + choicedCity.id + "/");
                        } else {
                            choicedRoom.store(item);
                            myRef.child(PATH_USERS_ROOMS + uid + "/" + choicedRoom.id).setValue(new Room(choicedCity, choicedRoom));
                            Intent intent = new Intent(getApplicationContext(), ChatView.class);
                            intent.putExtra(PUTEXTRA_IDROOM, choicedRoom.id);
                            intent.putExtra(PUTEXTRA_NAMEROOM, choicedRoom.name);
                            intent.putExtra(PUTEXTRA_IDCITY, choicedCity.id);
                            intent.putExtra(PUTEXTRA_NAMECITY, choicedCity.name);
                            intent.putExtra(PUTEXTRA_UID, uid);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        };
        if (isCityChoiced) {
            recyclerView.swapAdapter(adapterSelect, false);
        } else {
            recyclerView.setAdapter(adapterSelect);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isCityChoiced) {
            isCityChoiced = false;
            startFirebaseRecycleAdapterChoiceItem(PATH_CATALOG_CITY);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_search, menu);

        MenuItem item = menu.findItem(R.id.search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
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

    public void goAuth(View v) {
        startActivity(new Intent(SelectChat.this, GoogleLoginActivity.class));
        finish();
    }

    private void addRoomDialog() {
        final EditText addRoomV = new EditText(this);
        addRoomV.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(addRoomV)
                .setMessage(R.string.add_room)
                .setPositiveButton(R.string.add_room_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choicedRoom.name = addRoomV.getText().toString();
                        addRoomToBase();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.add_room_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void addRoomToBase() {
        if (choicedRoom.name.length() > 0) {
            myRef.child(PATH_SETTINGS_NEXTIDROOM).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    NextRoom nextRoom = dataSnapshot.getValue(NextRoom.class);
                    choicedRoom.id = nextRoom.nextIdRoom;
                    choicedRoom.payload = System.currentTimeMillis(); // time create room
                    choicedRoom.iconPath = NO_ICON;
                    myRef.child(PATH_CATALOG_ROOMS + choicedCity.id + "/" + choicedRoom.id + "/").setValue(choicedRoom);
                    choicedCity.payload = choicedCity.payload + 1; // we have a new chat
                    myRef.child(PATH_CATALOG_CITY + choicedCity.id + "/").setValue(choicedCity);
                    nextRoom.increase();
                    myRef.child(PATH_SETTINGS_NEXTIDROOM).setValue(nextRoom);
                    myRef.child(PATH_USERS_ROOMS + uid + "/" + choicedRoom.id).setValue(new Room(choicedCity, choicedRoom));
                    Intent intent = new Intent(getApplicationContext(), ChatView.class);
                    intent.putExtra(PUTEXTRA_IDROOM, choicedRoom.id);
                    intent.putExtra(PUTEXTRA_NAMEROOM, choicedRoom.name);
                    intent.putExtra(PUTEXTRA_IDCITY, choicedCity.id);
                    intent.putExtra(PUTEXTRA_NAMECITY, choicedCity.name);
                    intent.putExtra(PUTEXTRA_UID, uid);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(SelectChat.this, R.string.database_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void addCityToBase() {
        myRef.child(PATH_SETTINGS_NEXTIDCITY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                NextCity nextCity = dataSnapshot.getValue(NextCity.class);
                choicedCity.id = nextCity.nextIdCity;
                choicedCity.payload = 0; // no chat in new city
                choicedCity.iconPath = NO_ICON;
                myRef.child(PATH_CATALOG_CITY + choicedCity.id + "/").setValue(choicedCity);
                nextCity.increase();
                myRef.child(PATH_SETTINGS_NEXTIDCITY).setValue(nextCity);
                isCityChoiced = true;
                startFirebaseRecycleAdapterChoiceItem(PATH_CATALOG_ROOMS + choicedCity.id + "/");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SelectChat.this, R.string.database_error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addIfNewCity(final String myCity) {
        myRef.child(PATH_CATALOG_CITY).orderByChild(CHILD_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isNewCity = true;
                choicedCity.name = myCity;
                for (DataSnapshot name : dataSnapshot.getChildren()) {
                    if (name.child(CHILD_NAME).getValue().equals(myCity)) {
                        choicedCity.id = (long) name.child(CHILD_ID).getValue();
                        isNewCity = false;
                        break;
                    }
                }
                if (isNewCity) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SelectChat.this);
                    builder.setMessage(getResources().getString(R.string.add_city) + myCity)
                            .setCancelable(false)
                            .setPositiveButton(R.string.add_city_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    addCityToBase();
                                }
                            })
                            .setNegativeButton(R.string.add_city_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();

                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Toast.makeText(SelectChat.this, R.string.already_have_city + choicedCity.name, Toast.LENGTH_LONG).show();
                    isCityChoiced = true;
                    startFirebaseRecycleAdapterChoiceItem(PATH_CATALOG_ROOMS + choicedCity.id + "/");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SelectChat.this, R.string.database_error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupLocationServices() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void permissionGrantDialog() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                setPermisionGrantedFlag(true);
                setupLocationServices();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(SelectChat.this, R.string.permission_deny + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                setPermisionGrantedFlag(false);
            }
        };
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(R.string.i_need_permision)
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
    }

    private void setPermisionGrantedFlag(boolean isGranted) {
        isPermisionGranted = isGranted;
    }

    @Override
    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION})
    public void onConnected(Bundle connectionHint) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionGrantDialog();
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();
            mGoogleApiClient.disconnect();
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null) {
                    String myCity = (addresses.get(0).getLocality());
                    if (myCity.length() > 0) {
                        addIfNewCity(myCity);
                    }
                } else {
                    Toast.makeText(this, R.string.no_last_location, Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.no_last_location, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Toast.makeText(SelectChat.this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

}