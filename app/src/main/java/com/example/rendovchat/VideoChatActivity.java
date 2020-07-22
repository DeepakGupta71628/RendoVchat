package com.example.rendovchat;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements  Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY="46756342";
    private static  String SESSION_ID="1_MX40Njc1NjM0Mn5-MTU5MDA5NDgzODE5Nn5uREtXQW5leGlCQlhXK3l0MUp2S3QxSWl-fg";
    private static  String TOKEN="T1==cGFydG5lcl9pZD00Njc1NjM0MiZzaWc9NjhlZTFhZDEwZDVjZjkyOWQ1ZTVlYTc3M2FhNTQzN2Q1MjczZTYyOTpzZXNzaW9uX2lkPTFfTVg0ME5qYzFOak0wTW41LU1UVTVNREE1TkRnek9ERTVObjV1UkV0WFFXNWxlR2xDUWxoWEszbDBNVXAyUzNReFNXbC1mZyZjcmVhdGVfdGltZT0xNTkwMDk0ODk1Jm5vbmNlPTAuMTUyNTM0MzA3ODUxMjA2MDMmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5MDExNjQ5NCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG=VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM=124;

    private ImageView closeVideoChatBtn;
    private DatabaseReference userRef;
    private String userId="";


    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userRef= FirebaseDatabase.getInstance().getReference().child("User");
        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        closeVideoChatBtn=findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(userId).hasChild("Ringing")){
                            userRef.child(userId).child("Ringing").removeValue();
                            if(mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if(mPublisher!=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();
                        }

                        if(dataSnapshot.child(userId).hasChild("Calling")){
                            userRef.child(userId).child("Calling").removeValue();
                            if(mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if(mPublisher!=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();
                        }
                        else{
                            if(mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if(mPublisher!=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this,RegistrationActivity.class));
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermissinon();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissinon(){
        String[] perms={Manifest.permission.INTERNET,Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this,perms)){
            mPublisherViewController=findViewById(R.id.publisher_container);
            mSubscriberViewController=findViewById(R.id.subcriber_container);
            //1. initislize to the session
            mSession= new Session(this,API_KEY,SESSION_ID);

            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }
        else{
            EasyPermissions.requestPermissions(this,"Hey, this app needs  Mic and Camera, Plese allow. ",RC_VIDEO_APP_PERM,perms);
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {


    }

    //2.to publishing the session
    @Override
    public void onConnected(Session session) {

        Log.i(LOG_TAG,"Sesssion Connected");
        mPublisher=new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);
        mPublisherViewController.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {

        Log.i(LOG_TAG,"Stream Disconnected");
    }

    //3.subscribing to the Stream
    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.i(LOG_TAG,"Stream Reciving");
        if(mSubscriber==null){
            mSubscriber=new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        Log.i(LOG_TAG,"Stream Droped");
        if(mSubscriber!=null){
            mSubscriber=null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

        Log.i(LOG_TAG,"Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
