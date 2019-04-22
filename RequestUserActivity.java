package com.manan.dev.clubconnect;

import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.manan.dev.clubconnect.Adapters.RequestUserViewAdapter;
import com.manan.dev.clubconnect.Models.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestUserActivity extends Fragment {


    private RequestUserViewAdapter requestListRecyclerAdapter;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mDBRefCurEvent;
    private ChildEventListener mChildEventListCurEvent;

    private ArrayList<UserData> userIdArrayList;
    private Map<String, String> userIdList;
    private Map<String, UserData> allUsers;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_request_user, container, false);
        userIdArrayList = new ArrayList<>();
        userIdList = new HashMap<>();
        allUsers = new HashMap<>();

        RecyclerView requestRecyclerView = view.findViewById(R.id.request_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        requestRecyclerView.setLayoutManager(mLayoutManager);
        requestListRecyclerAdapter = new RequestUserViewAdapter(userIdArrayList, this.getContext());
        requestRecyclerView.setAdapter(requestListRecyclerAdapter);


        String clubName = this.getActivity().getIntent().getStringExtra("name");
        //clubName = "Manan";
        if(this.getActivity().getIntent().getExtras() != null){
            for(String key : this.getActivity().getIntent().getExtras().keySet()){
                if(key.equals("from")){
                    clubName = this.getActivity().getIntent().getExtras().getString(key);
                    clubName = clubName.substring(8);
                    //Toast.makeText(RequestUserActivity.this, key, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(RequestUserActivity.this, clubName, Toast.LENGTH_SHORT).show();
                }
            }
        }
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("notification").child(clubName);
        mDBRefCurEvent = FirebaseDatabase.getInstance().getReference().child("users");



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        attachDatabaseListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseListener();
        userIdArrayList.clear();
        userIdList.clear();
        allUsers.clear();
    }

    private void detachDatabaseListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mChildEventListCurEvent != null) {
            mDBRefCurEvent.removeEventListener(mChildEventListCurEvent);
            mChildEventListCurEvent = null;
        }
    }


    private void attachDatabaseListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        String refValue = dataSnapshot.getValue(String.class);
                        for (String key : userIdList.keySet())
                            if (userIdList.get(key).equals(refValue)) {
                                dataSnapshot.getRef().setValue(null);
                                return;
                            }
                        userIdList.put(dataSnapshot.getKey(), refValue);
                        modifyUserIdArrayList();
                        requestListRecyclerAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    try {
                        userIdList.remove(dataSnapshot.getKey());
                        modifyUserIdArrayList();
                        requestListRecyclerAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mDatabaseReference.addChildEventListener(mChildEventListener);
        }
        if (mChildEventListCurEvent == null) {
            mChildEventListCurEvent = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    try {
                        UserData u = dataSnapshot.getValue(UserData.class);
                        if (u != null) {
                            u.UID = dataSnapshot.getKey();
                            allUsers.put(dataSnapshot.getKey(), u);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    modifyUserIdArrayList();
                    requestListRecyclerAdapter.notifyDataSetChanged();

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    try {
                        UserData u = dataSnapshot.getValue(UserData.class);
                        if (u != null) {
                            u.UID = dataSnapshot.getKey();
                            allUsers.put(dataSnapshot.getKey(), u);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    modifyUserIdArrayList();
                    requestListRecyclerAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    try {
                        allUsers.remove(dataSnapshot.getKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    modifyUserIdArrayList();
                    requestListRecyclerAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mDBRefCurEvent.addChildEventListener(mChildEventListCurEvent);
        }


    }

    private void modifyUserIdArrayList() {
        userIdArrayList.clear();
        String uid;
        for (String pushId : userIdList.keySet()) {
            uid = userIdList.get(pushId);
            if (allUsers.containsKey(uid)) {
                allUsers.get(uid).tempData = pushId;
                userIdArrayList.add(allUsers.get(uid));
            }
        }
    }
}