package com.hooke.citychat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.InputType;

import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hooke.citychat.model.Chat;
import com.hooke.citychat.model.Room;

import java.text.SimpleDateFormat;

import java.util.UUID;

/**
 * Activity showing chat and will provide to adding some post
 */
public class ChatView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static final String PATH_CITYCHAT = "/citychat/";
    public static final String PATH_USERS_ROOMS = "/usersRooms/";
    public static final String PATH_CATALOG_ROOMS = "/catalog/rooms/";
    public static final String PATH_TIME_LAST_READ = "/timeLastRead";
    public static final String PATH_TIME_PAYLOAD = "/payload/";
    public static final String CHILD_TIMESTAMP = "timestamp";
    public static final int TWO_SECOND = 2000;
    public static final int ONE_MINUTE = 60000;
    public static final int ONE_DAY = 86400000;
    public static final int ONE_SECOND = 1000;
    RecyclerView recyclerView;
    private DatabaseReference myRef;
    static Context appContext;
    Room room;
    private String uid;
    public static final String EMPTY = "";
    public static final String MARKER_NOAUTH = "noauth";
    public static final String PUTEXTRA_UID = "uid";
    public static final String PUTEXTRA_IDROOM = "idRoom";
    public static final String PUTEXTRA_NAMEROOM = "nameRoom";
    public static final String PUTEXTRA_IDCITY = "idCity";
    public static final String PUTEXTRA_NAMECITY = "nameCity";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    SimpleDateFormat dfDateTime = new SimpleDateFormat(DATE_TIME_FORMAT);
    SimpleDateFormat dTime = new SimpleDateFormat(TIME_FORMAT);
    private String myName;
    EditText postText;
    ImageButton postButton;
    ActionMenuItemView lockButton;
    private static String password;
    private boolean isSetPassword = false;

    private ShareActionProvider mShareActionProvider;


    FirebaseRecyclerAdapter<Chat, ChatView.ChatViewHolder> adapterChat;

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nAuthor;
        TextView nTimeDate;
        TextView nChatText;
        CardView mRoomCardView;
        CardView mTextCard;

        public ChatViewHolder(View itemView) {
            super(itemView);
            nAuthor = (TextView) itemView.findViewById(R.id.author);
            nTimeDate = (TextView) itemView.findViewById(R.id.time_date);
            nChatText = (TextView) itemView.findViewById(R.id.chat_text);
            mRoomCardView = (CardView) itemView.findViewById(R.id.chat_card_view);
            mTextCard = (CardView) itemView.findViewById(R.id.chat_text_card);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_chat);
        appContext = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_for_chat);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        postButton = (ImageButton) findViewById(R.id.post_button);
        postText = (EditText) findViewById(R.id.post_text);
        room = new Room(getIntent().getExtras().getString(PUTEXTRA_NAMECITY), getIntent().getExtras().getLong(PUTEXTRA_IDCITY),
                getIntent().getExtras().getString(PUTEXTRA_NAMEROOM), getIntent().getExtras().getLong(PUTEXTRA_IDROOM), 0, "-");
        uid = getIntent().getExtras().getString(PUTEXTRA_UID);
        setTitle(getString(R.string.chat_view) + " " + room.nameCity + ":" + room.nameRoom);
        startFirebaseRecycleAdapter();
        myName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    private void startFirebaseRecycleAdapter() {
        myRef = FirebaseDatabase.getInstance().getReference();
        recyclerView = (RecyclerView) findViewById(R.id.rv_list_chat);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        adapterChat = new FirebaseRecyclerAdapter<Chat, ChatView.ChatViewHolder>(
                Chat.class,
                R.layout.items_list_chat,
                ChatView.ChatViewHolder.class,
                myRef.child(PATH_CITYCHAT + room.idRoom + "/").orderByChild(CHILD_TIMESTAMP)
        ) {
            @Override
            protected void populateViewHolder(final ChatView.ChatViewHolder viewHolder, final Chat chat, final int position) {
                viewHolder.nAuthor.setText(chat.author);
                long msecFromTimePost = System.currentTimeMillis() - chat.timestamp;
                if (msecFromTimePost < TWO_SECOND) {
                    viewHolder.nTimeDate.setText(R.string.post_just_now);
                } else if (msecFromTimePost < ONE_MINUTE) {
                    viewHolder.nTimeDate.setText((int) msecFromTimePost / ONE_SECOND + " " + getResources().getString(R.string.seconds_ago));
                } else if (msecFromTimePost < ONE_DAY) {
                    viewHolder.nTimeDate.setText(getResources().getString(R.string.less_day_ago) + " " + dTime.format(chat.timestamp));
                } else {
                    viewHolder.nTimeDate.setText(dfDateTime.format(chat.timestamp));
                }
                if (chat.crypted) {
                    viewHolder.mTextCard.setCardBackgroundColor(getResources().getColor(R.color.card_crypted));
                    if (isSetPassword) {
                        viewHolder.nChatText.setText(decode(chat.text));
                    } else {
                        viewHolder.nChatText.setText(getResources().getString(R.string.message_is_crypted));
                    }
                } else {
                    viewHolder.mTextCard.setCardBackgroundColor(getResources().getColor(R.color.card_clear));
                    viewHolder.nChatText.setText(chat.text);
                }

                viewHolder.mRoomCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
                if (uid.equals(chat.uid)) {
                    viewHolder.nAuthor.setTextColor(getResources().getColor(R.color.authot_is_me));
                }
                recyclerView.smoothScrollToPosition(position + 1);
                long justNow = System.currentTimeMillis();
                myRef.child(PATH_USERS_ROOMS + uid + "/" + room.idRoom + PATH_TIME_LAST_READ).setValue(justNow);
            }
        };
        recyclerView.setAdapter(adapterChat);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
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
        getMenuInflater().inflate(R.menu.main_crypto, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        doShare();
        return true;
    }

    private void doShare() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_app) +
                getResources().getString(R.string.share_room) + room.nameRoom + "@" + room.nameCity);
        mShareActionProvider.setShareIntent(sendIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.crypto) {
            final EditText getPassword = new EditText(this);
            getPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(getPassword)
                    .setMessage(R.string.set_password)
                    .setPositiveButton(R.string.set_password_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            lockButton = (ActionMenuItemView) findViewById(R.id.crypto);
                            lockButton.setIcon(getResources().getDrawable(R.drawable.lock));
                            password = getPassword.getText().toString();
                            isSetPassword = true;
                            adapterChat.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.set_password_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            builder.show();
        }
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
        startActivity(new Intent(ChatView.this, GoogleLoginActivity.class));
        finish();
    }

    public void post(View v) {
        if (uid.equals(MARKER_NOAUTH)) {
            loginDialog();
        } else if (postText.getText().length() > 0) {
            if (isSetPassword) {
                sayToBase(encode(postText.getText().toString()));
            } else {
                sayToBase(postText.getText().toString());
            }
            postText.setText(EMPTY);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(postButton.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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

    public void sayToBase(String sayChat) {
        if (sayChat.length() > 0) {
            myRef.child(PATH_CITYCHAT + room.idRoom + "/" + UUID.randomUUID().toString() + "/")
                    .setValue(new Chat(myName, sayChat, System.currentTimeMillis(), uid, isSetPassword));
            if (recyclerView.getAdapter().getItemCount() > 0) {
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
            }
            long justNow = System.currentTimeMillis();
            myRef.child(PATH_USERS_ROOMS + uid + "/" + room.idRoom + PATH_TIME_LAST_READ).setValue(justNow);
            myRef.child(PATH_CATALOG_ROOMS + room.idCity + "/" + room.idRoom + PATH_TIME_PAYLOAD).setValue(justNow);
        }
    }

    public static String encode(String plainText) {
        char [] plainTextCharArray = plainText.toCharArray();
        byte [] txt = new byte[plainTextCharArray.length*2];
        int pointerTxt = 0;
        for (char eachone: plainTextCharArray) {
            short value = (short) eachone;
            txt[pointerTxt++] = (byte)(value & 0xff);
            txt[pointerTxt++] = (byte)((value >> 8) & 0xff);
        }
        byte[] key = password.getBytes();
        byte[] res = new byte[txt.length];
        for (int i = 0; i < txt.length; i++) {
            res[i] = (byte) (txt[i] ^ key[i % key.length]);
        }
        return Base64.encodeToString(res, Base64.DEFAULT);
    }

    public static String decode(String cryptedText) {
        byte[] pText = Base64.decode(cryptedText, Base64.DEFAULT);
        byte[] res = new byte[pText.length];
        byte[] key = password.getBytes();
        for (int i = 0; i < pText.length; i++) {
            res[i] = (byte) (pText[i] ^ key[i % key.length]);
        }
        char [] txt = new char[res.length/2];
        int pointerRes = 0;
        for (int i=0; i<txt.length; i++){
            txt [i] = (char) (res [pointerRes++] + (res [pointerRes++] << 8));
        }
        return new String(txt);
    }
}

