package com.example.videoreelsapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.videoreelsapp.R
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

class VideoCallFragment : Fragment() {

    // Replace these with your real values
    private val myAppId = "27a8437360224b40a5eb711eef6c3137"
    private val channelName = "A"
    private val token = "007eJxTYOD+sHMJv5XQ6U36AuL5yZkpJhz/9PIXbt4Us4KxJWxGf6sCg5F5ooWJsbmxmYGRkUmSiUGiaWqSuaFhamqaWbKxobH5z+UzMxoCGRkOBLxmZWSAQBCfkcGRgQEAO1YcfQ=="

    private var mRtcEngine: RtcEngine? = null
    private val PERMISSION_REQ_ID = 22

    private lateinit var localContainer: FrameLayout
    private lateinit var remoteContainer: FrameLayout

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            activity?.runOnUiThread { showToast("Joined channel: $channel") }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            activity?.runOnUiThread {
                showToast("User joined: $uid")
                setupRemoteVideo(uid)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            activity?.runOnUiThread {
                showToast("User offline: $uid")
                remoteContainer.removeAllViews()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_video_call, container, false)
        localContainer = v.findViewById(R.id.local_video_view_container)
        remoteContainer = v.findViewById(R.id.remote_video_view_container)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (checkPermissions()) {
            startVideoCalling()
        } else {
            requestPermissions()
        }
    }

    private fun initializeRtcEngine() {
        try {
            val config = RtcEngineConfig().apply {
                mContext = requireContext().applicationContext
                mAppId = myAppId
                mEventHandler = mRtcEventHandler
            }
            mRtcEngine = RtcEngine.create(config)
        } catch (e: Exception) {
            throw RuntimeException("Error initializing RTC engine: ${e.message}")
        }
    }

    private fun startVideoCalling() {
        initializeRtcEngine()

        // enable video + preview
        mRtcEngine?.enableVideo()
        mRtcEngine?.startPreview()

        // --- Set encoder configuration
        // Use enum form (FRAME_RATE enum) — if your SDK expects Int instead, replace FRAMERATE enum with .value
        try {
            mRtcEngine?.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,    // <- enum form
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
            )
        } catch (ex: NoSuchMethodError) {
            // Rare: constructor signatures differ by SDK version. If you get a runtime error here, remove this block
            ex.printStackTrace()
        }

        setupLocalVideo()
        joinChannel()
    }

    private fun setupLocalVideo() {
        // add local preview (small overlay)
        val surfaceView = SurfaceView(requireContext()).apply {
            setZOrderMediaOverlay(true)
        }
        localContainer.removeAllViews()
        localContainer.addView(surfaceView)
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun setupRemoteVideo(uid: Int) {
        // clear previous
        remoteContainer.removeAllViews()
        val surfaceView = SurfaceView(requireContext())
        remoteContainer.addView(surfaceView)
        mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    private fun joinChannel() {
        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
            publishCameraTrack = true
        }
        mRtcEngine?.joinChannel(token, channelName, 0, options)
    }

    /* ---------- permissions ---------- */
    private fun checkPermissions(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) return false
        }
        return true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), getRequiredPermissions(), PERMISSION_REQ_ID)
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQ_ID && checkPermissions()) {
            startVideoCalling()
        } else {
            showToast("Permissions required")
        }
    }

    private fun cleanupAgoraEngine() {
        mRtcEngine?.apply {
            stopPreview()
            leaveChannel()
        }
        RtcEngine.destroy()
        mRtcEngine = null
    }

    private fun showToast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupAgoraEngine()
    }
}







