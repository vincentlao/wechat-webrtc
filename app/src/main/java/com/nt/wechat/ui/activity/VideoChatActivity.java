package com.nt.wechat.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nt.wechat.R;
import com.nt.wechat.entity.Contact;
import com.nt.wechat.entity.Message;
import com.nt.wechat.services.WeChatService;
import com.nt.wechat.services.interfaces.IMessageManagerService;
import com.nt.wechat.util.CommonDefine;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VideoChatActivity extends BaseActivity implements PeerConnection.Observer, SdpObserver, IMessageManagerService.IMessageListener {

    /**
     * AudioDevice is the names of possible audio devices that we currently
     * support.
     */
    // TODO(henrika): add support for BLUETOOTH as well.
    public enum AudioDevice {
        SPEAKER_PHONE,
        WIRED_HEADSET,
        EARPIECE,
    }

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final String TAG = "wechat";

    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private static final String LOCAL_MEDIA_STREAM_ID = "ARDAMS";

    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_CODEC_H264 = "H264";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT= "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT  = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String MAX_VIDEO_WIDTH_CONSTRAINT = "maxWidth";
    private static final String MIN_VIDEO_WIDTH_CONSTRAINT = "minWidth";
    private static final String MAX_VIDEO_HEIGHT_CONSTRAINT = "maxHeight";
    private static final String MIN_VIDEO_HEIGHT_CONSTRAINT = "minHeight";
    private static final String MAX_VIDEO_FPS_CONSTRAINT = "maxFrameRate";
    private static final String MIN_VIDEO_FPS_CONSTRAINT = "minFrameRate";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;
    private static final int MAX_VIDEO_WIDTH = 1280;
    private static final int MAX_VIDEO_HEIGHT = 1280;
    private static final int MAX_VIDEO_FPS = 30;


    private VideoSource localVideoSource;
    private VideoRenderer localRender;
    private VideoRenderer remoteRender;
    private GLSurfaceView videoView;
    private VideoCapturer capturer;
    private PeerConnection peerConnection;
    private AudioManager audioManager;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private boolean savedIsSpeakerPhoneOn = false;
    private boolean savedIsMicrophoneMute = false;
    private  int currVolume = 0;

    // Contains the currently selected audio device.
    private AudioDevice selectedAudioDevice;

    // Contains a list of available audio devices. A Set collection is used to
    // avoid duplicate elements.
    private final Set<AudioDevice> audioDevices = new HashSet<AudioDevice>();

    // For now; always use the speaker phone as default device selection when
    // there is a choice between SPEAKER_PHONE and EARPIECE.
    // TODO(henrika): it is possible that EARPIECE should be preferred in some
    // cases. If so, we should set this value at construction instead.
    private final AudioDevice defaultAudioDevice = AudioDevice.SPEAKER_PHONE;


    private SessionDescription localSdp; // either offer or answer SDP

    private String contactJid;
    private boolean initiator;

    private Button handupButton;

    private WeChatService.WeChatBinder binder;

    private SessionDescription sdpOffer;

    private LinkedList<IceCandidate> queuedRemoteCandidates = new LinkedList<IceCandidate>();
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_chat);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.gl_surface);
        videoView = (GLSurfaceView)mContentView;


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        handupButton = (Button)findViewById(R.id.handup_button);
        //handupButton.setOnTouchListener(mDelayHideTouchListener);
        handupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent i = new Intent(VideoChatActivity.this, MainActivity.class);
                startActivity(i);
            }
        });


        try {

            audioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));

            // Store current audio state so we can restore it when close() is called.
            savedAudioMode = audioManager.getMode();
            savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
            savedIsMicrophoneMute = audioManager.isMicrophoneMute();

            // Request audio focus before making any device switch.
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

            // Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
            // required to be in this mode when playout and/or recording starts for
            // best possible VoIP performance.
            // TODO(henrika): we migh want to start with RINGTONE mode here instead.
            audioManager.setMode(AudioManager.MODE_IN_CALL);

            // Always disable microphone mute during a WebRTC call.
            setMicrophoneMute(false);

            // Do initial selection of audio device. This setting can later be changed
            // either by adding/removing a wired headset or by covering/uncovering the
            // proximity sensor.
            updateAudioDeviceState(hasWiredHeadset());

            OpenSpeaker();

            boolean speakerOn = audioManager.isSpeakerphoneOn();
            audioManager.setSpeakerphoneOn(true);

            // First, we initiate the PeerConnectionFactory with our application context and some options.
            PeerConnectionFactory.initializeAndroidGlobals(this,
                    true,
                    true,
                    true); // Render EGL Context

            PeerConnectionFactory pcFactory = new PeerConnectionFactory();


            String deviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
            // Creates a VideoCapturerAndroid instance for the device name
            capturer = VideoCapturerAndroid.create(deviceName);


            // First create a Video Source, then we can make a Video Track
            localVideoSource = pcFactory.createVideoSource(capturer, defaultVideoConstraints());
            VideoTrack localVideoTrack = pcFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
            localVideoTrack.setEnabled(true);
            // First we create an AudioSource then we can create our AudioTrack
            AudioSource audioSource = pcFactory.createAudioSource(defaultAudioConstraints());
            AudioTrack localAudioTrack = pcFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
            localAudioTrack.setEnabled(true);

            // To create our VideoRenderer, we can use the included VideoRendererGui for simplicity
            // First we need to set the GLSurfaceView that it should render to
            this.videoView = (GLSurfaceView) findViewById(R.id.gl_surface);

            //videoView.setRenderer(new MyRender());
            // Then we set that view, and pass a Runnable to run once the surface is ready
            VideoRendererGui.setView(videoView, null);

            // Now that VideoRendererGui is ready, we can get our VideoRenderer.
            // IN THIS ORDER. Effects which is on top or bottom
            //remoteRender = VideoRendererGui.create(0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, false);
            remoteRender = VideoRendererGui.createGui(0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);
            localRender = VideoRendererGui.createGui(60, 0, 40, 40, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);

            localVideoTrack.addRenderer(localRender);
            // We start out with an empty MediaStream object, created with help from our PeerConnectionFactory
            //  Note that LOCAL_MEDIA_STREAM_ID can be any string
            //
            MediaStream mediaStream = pcFactory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);

            // Now we can add our tracks.
            mediaStream.addTrack(localVideoTrack);
            mediaStream.addTrack(localAudioTrack);

            peerConnection = pcFactory.createPeerConnection(defaultIceServers(), defaultPcConstraints(), this);
            peerConnection.addStream(mediaStream);




            initiator = true;
            Bundle extras = getIntent().getExtras();
            if (extras.getInt(CommonDefine.JSON_MESSAGE_SUB_TYPE, 0) == CommonDefine.MessageSubType_Offer) {
                initiator = false;
            }

            contactJid = extras.getString(CommonDefine.JSON_JID);//"zhangsufen@xmpp.jp";//

            if (!initiator) {
                sdpOffer = new SessionDescription(SessionDescription.Type.fromCanonicalForm(extras.getString(CommonDefine.JSON_TYPE)), extras.getString(CommonDefine.JSON_SDP));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    void onBackendConnected() {
        binder = getBinder();
        if (binder == null) {
            return;
        }

        binder.getMessageManagerService().addMessageListener(this);

        if (initiator) {
            initiateoffer();
        } else {
            offerReceived();
        }
    }

    @Override
    protected void refreshUiReal() {

    }


    private void offerReceived() {
        if (peerConnection == null || sdpOffer == null) {
            return;
        }

        String sdpDescription = sdpOffer.description;
        sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
        sdpDescription = preferCodec(sdpDescription, VIDEO_CODEC_VP8, false);

        //if (peerConnectionParameters.audioStartBitrate > 0) {
        sdpDescription = setStartBitrate(AUDIO_CODEC_OPUS, false, sdpDescription, 32);
        //}

        Log.d(TAG, "Set remote SDP.");

        SessionDescription sdpRemote = new SessionDescription(sdpOffer.type, sdpDescription);
        peerConnection.setRemoteDescription(this, sdpRemote);

        peerConnection.createAnswer(this, defaultPcConstraints());
    }

    private void initiateoffer() {
        peerConnection.createOffer(this, defaultPcConstraints());
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.videoView.onPause();
        this.localVideoSource.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.videoView.onResume();
        this.localVideoSource.restart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.localVideoSource != null) {
            this.localVideoSource.stop();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private static MediaConstraints defaultPcConstraints(){
        MediaConstraints pcConstraints = new MediaConstraints();
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        return pcConstraints;
    }

    private static MediaConstraints defaultVideoConstraints(){
        MediaConstraints videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", "1280"));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", "720"));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minWidth", "640"));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minHeight", "480"));
        return videoConstraints;
    }

    private static MediaConstraints defaultAudioConstraints(){
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT , "true"));
        return audioConstraints;
    }

    public static List<PeerConnection.IceServer> defaultIceServers(){
        List<PeerConnection.IceServer> iceServers = new ArrayList<PeerConnection.IceServer>(25);
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.services.mozilla.com"));
        iceServers.add(new PeerConnection.IceServer("turn:turn.bistri.com:80", "homeo", "homeo"));
        iceServers.add(new PeerConnection.IceServer("turn:turn.anyfirewall.com:443?transport=tcp", "webrtc", "webrtc"));

        // Extra Defaults - 19 STUN servers + 4 initial = 23 severs (+2 padding) = Array cap 25
        iceServers.add(new PeerConnection.IceServer("stun:stun1.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun2.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun3.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun4.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun01.sipphone.com"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.ekiga.net"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.fwdnet.net"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.ideasip.com"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.iptel.org"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.rixtelecom.se"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.schlund.de"));
        iceServers.add(new PeerConnection.IceServer("stun:stunserver.org"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.softjoys.com"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.voiparound.com"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.voipbuster.com"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.voipstunt.com"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.voxgratia.org"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.xten.com"));

        return iceServers;
    }

    @Override
    public void onSignalingChange(final PeerConnection.SignalingState signalingState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "SignalingState: " + signalingState);
            }
        });
    }

    @Override
    public void onIceConnectionChange(final PeerConnection.IceConnectionState newState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "IceConnectionState: " + newState);
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    Log.d(TAG, "IceConnectionState: connected" );
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    Log.d(TAG, "IceConnectionState: disconnected");
                } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                    Log.d(TAG, "IceConnectionState: connect failed");
                }
            }
        });
    }

    @Override
    public void onIceConnectionReceivingChange(final boolean b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onIceConnectionReceivingChange changed to " + b);
            }
        });
    }

    @Override
    public void onIceGatheringChange(final PeerConnection.IceGatheringState iceGatheringState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "IceConnectionReceiving changed to " + iceGatheringState);
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put(CommonDefine.JSON_JID, binder.getAccountManagerService().getAccount().getJid());
                    payload.put(CommonDefine.JSON_SDPMLINEINDEX, candidate.sdpMLineIndex);
                    payload.put(CommonDefine.JSON_SDPMID, candidate.sdpMid);
                    payload.put(CommonDefine.JSON_CONDIDATE, candidate.sdp);
                    binder.getMessageManagerService().sendMessage(contactJid, payload.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        final MediaStream remoteStream = mediaStream;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (remoteStream.audioTracks.size() == 0 || remoteStream.videoTracks.size() == 0) {
                        return;
                    }

                    remoteStream.videoTracks.get(0).addRenderer(remoteRender);
                    remoteStream.audioTracks.get(0).setEnabled(true);
                    remoteStream.videoTracks.get(0).setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRemoveStream(final  MediaStream mediaStream) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mediaStream.videoTracks.isEmpty()) {
                    mediaStream.videoTracks.get(0).dispose();
                }

                if (!mediaStream.audioTracks.isEmpty()) {
                    mediaStream.audioTracks.get(0).dispose();
                }
            }
        });
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void onRenegotiationNeeded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void onCreateSuccess(final SessionDescription origSdp) {
        if (localSdp != null) {
            return;
        }

        String sdpDescription = origSdp.description;
        sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
        sdpDescription = preferCodec(sdpDescription, VIDEO_CODEC_VP8, false);
        final SessionDescription sdp = new SessionDescription(origSdp.type, sdpDescription);

        localSdp = sdp;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                peerConnection.setLocalDescription(VideoChatActivity.this, sdp);
            }
        });
    }

    @Override
    public void onSetSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (initiator) {
                    // For offering peer connection we first create offer and set
                    // local SDP, then after receiving answer set remote SDP.
                    if (peerConnection.getRemoteDescription() == null) {
                        // We've just set our local SDP so time to send it.
                        SendLocalSdp();
                    } else {
                        // We've just set remote description, so drain remote
                        // and send local ICE candidates.
                        drainRemoteCandidates();
                    }
                } else {
                    // For answering peer connection we set remote SDP and then
                    // create answer and set local SDP.
                    if (peerConnection.getLocalDescription() != null) {
                        // We've just set our local SDP so time to send it, drain
                        // remote and send local ICE candidates.
                        SendLocalSdp();
                    } else {
                        // We've just set remote SDP - do nothing for now -
                        // answer will be created soon.
                        //Log.d(TAG, "Remote SDP set succesfully");
                        drainRemoteCandidates();
                    }
                }
            }
        });
    }

    @Override
    public void onCreateFailure(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void onSetFailure(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void processMessage(final String senderJid, final org.jivesoftware.smack.packet.Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String body = message.getBody();
                    JSONObject jsonMessage = new JSONObject(body);

                    String jidPeer = jsonMessage.getString(CommonDefine.JSON_JID);
                    if (!jidPeer.equals(contactJid)) {
                        return;
                    }

                    if (jsonMessage.has(CommonDefine.JSON_SDP)) {
                        receiveAnswer(jsonMessage);
                    } else if (jsonMessage.has(CommonDefine.JSON_CONDIDATE)) {
                        addIceCandidateAction(jsonMessage);
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void addIceCandidateAction(JSONObject payload) {
        try {
            IceCandidate candidate = new IceCandidate(
                    payload.getString(CommonDefine.JSON_SDPMID),
                    payload.getInt(CommonDefine.JSON_SDPMLINEINDEX),
                    payload.getString(CommonDefine.JSON_CONDIDATE)
            );

            if (peerConnection.getRemoteDescription() != null) {
                peerConnection.addIceCandidate(candidate);
            } else {
                queuedRemoteCandidates.add(candidate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveAnswer(JSONObject payload) {
        try {
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(payload.getString(CommonDefine.JSON_TYPE)), payload.getString(CommonDefine.JSON_SDP));

            String sdpDescription = sdp.description;
            sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
            sdpDescription = preferCodec(sdpDescription, VIDEO_CODEC_VP8, false);

            //if (peerConnectionParameters.audioStartBitrate > 0) {
            sdpDescription = setStartBitrate(AUDIO_CODEC_OPUS, false, sdpDescription, 32);
            //}

            Log.d(TAG, "Set remote SDP.");

            SessionDescription sdpRemote = new SessionDescription(sdp.type, sdpDescription);
            peerConnection.setRemoteDescription(this, sdpRemote);

            //peerConnection.setRemoteDescription(this, sdp);
            //drainRemoteCandidates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drainRemoteCandidates() {
        Log.e("demoActivity", " drainRemoteCandidates");
        if (queuedRemoteCandidates == null) {
            return;
        }

        for (int i = 0; i < queuedRemoteCandidates.size(); i++) {
            peerConnection.addIceCandidate(queuedRemoteCandidates.get(i));
        }

        queuedRemoteCandidates = null;
    }

    /** Sets the microphone mute state. */
    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (wasMuted == on) {
            return;
        }
        audioManager.setMicrophoneMute(on);
    }

    //打开扬声器
    private void OpenSpeaker() {
        try{
            //audioManager.setMode(AudioManager.ROUTE_SPEAKER);
            currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

            if(!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);

                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL ),
                        AudioManager.STREAM_VOICE_CALL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //关闭扬声器
    private void CloseSpeaker() {
        try {
            if(audioManager != null) {
                if(audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume, AudioManager.STREAM_VOICE_CALL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Toast.makeText(context,"揚聲器已經關閉",Toast.LENGTH_SHORT).show();
    }



    /** Gets the current earpiece state. */
    private boolean hasEarpiece() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over
     * the wired headset as audio routing depends on other conditions. We
     * only use it as an early indicator (during initialization) of an attached
     * wired headset.
     */
    @Deprecated
    private boolean hasWiredHeadset() {
        return audioManager.isWiredHeadsetOn();
    }

    /** Update list of possible audio devices and make new device selection. */
    private void updateAudioDeviceState(boolean hasWiredHeadset) {
        // Update the list of available audio devices.
        audioDevices.clear();
        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            audioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            audioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece())  {
                audioDevices.add(AudioDevice.EARPIECE);
            }
        }

        // Switch to correct audio device given the list of available audio devices.
        if (hasWiredHeadset) {
            setAudioDevice(AudioDevice.WIRED_HEADSET);
        } else {
            setAudioDevice(defaultAudioDevice);
        }
    }

    /** Changes selection of the currently active audio device. */
    public void setAudioDevice(AudioDevice device) {
        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                selectedAudioDevice = AudioDevice.SPEAKER_PHONE;
                break;
            case EARPIECE:
                setSpeakerphoneOn(false);
                selectedAudioDevice = AudioDevice.EARPIECE;
                break;
            case WIRED_HEADSET:
                setSpeakerphoneOn(false);
                selectedAudioDevice = AudioDevice.WIRED_HEADSET;
                break;
            default:
                break;
        }
    }

    /** Sets the speaker phone mode. */
    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        audioManager.setSpeakerphoneOn(on);
    }

    private void SendLocalSdp() {
        try {
            if (localSdp == null) {
                return;
            }

            JSONObject payload = new JSONObject();
            payload.put(CommonDefine.JSON_MESSAGE_TYPE, CommonDefine.MessageType_Video);
            if (initiator) {
                payload.put(CommonDefine.JSON_MESSAGE_SUB_TYPE, CommonDefine.MessageSubType_Offer);
            } else {
                payload.put(CommonDefine.JSON_MESSAGE_SUB_TYPE, CommonDefine.MessageSubType_Answer);
            }

            payload.put(CommonDefine.JSON_JID, binder.getAccountManagerService().getAccount().getJid());
            payload.put(CommonDefine.JSON_TYPE, localSdp.type.canonicalForm());
            payload.put(CommonDefine.JSON_SDP, localSdp.description);

            binder.getMessageManagerService().sendMessage(contactJid, payload.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        String[] lines = sdpDescription.split("\r\n");
        int mLineIndex = -1;
        String codecRtpMap = null;
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        String mediaDescription = "m=video ";
        if (isAudio) {
            mediaDescription = "m=audio ";
        }
        for (int i = 0; (i < lines.length)
                && (mLineIndex == -1 || codecRtpMap == null); i++) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i;
                continue;
            }
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                continue;
            }
        }
        if (mLineIndex == -1) {
            Log.w(TAG, "No " + mediaDescription + " line, so can't prefer " + codec);
            return sdpDescription;
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec);
            return sdpDescription;
        }
        Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + ", prefer at "
                + lines[mLineIndex]);
        String[] origMLineParts = lines[mLineIndex].split(" ");
        if (origMLineParts.length > 3) {
            StringBuilder newMLine = new StringBuilder();
            int origPartIndex = 0;
            // Format is: m=<media> <port> <proto> <fmt> ...
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(codecRtpMap);
            for (; origPartIndex < origMLineParts.length; origPartIndex++) {
                if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
                    newMLine.append(" ").append(origMLineParts[origPartIndex]);
                }
            }
            lines[mLineIndex] = newMLine.toString();
            Log.d(TAG, "Change media description: " + lines[mLineIndex]);
        } else {
            Log.e(TAG, "Wrong SDP media description format: " + lines[mLineIndex]);
        }
        StringBuilder newSdpDescription = new StringBuilder();
        for (String line : lines) {
            newSdpDescription.append(line).append("\r\n");
        }
        return newSdpDescription.toString();
    }


    private static String setStartBitrate(String codec, boolean isVideoCodec, String sdpDescription, int bitrateKbps) {
        String[] lines = sdpDescription.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        // Search for codec rtpmap in format
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec + " codec");
            return sdpDescription;
        }
        Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap
                + " at " + lines[rtpmapLineIndex]);

        // Check if a=fmtp string already exist in remote SDP for this codec and
        // update it with new bitrate parameter.
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " +  codec + " " + lines[i]);
                if (isVideoCodec) {
                    lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE
                            + "=" + bitrateKbps;
                } else {
                    lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE
                            + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            // Append new a=fmtp line if no such line exist for a codec.
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                if (isVideoCodec) {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " "
                            + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " "
                            + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }

        }
        return newSdpDescription.toString();
    }
}
