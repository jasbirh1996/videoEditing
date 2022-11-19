package com.stalmate.user.Helper

import android.content.Context
import android.content.Intent
import com.stalmate.user.view.authentication.FragmentOTPEnter
import com.stalmate.user.view.dashboard.ActivityDashboard
import com.stalmate.user.view.dashboard.Friend.ActivityCategoryCreate
import com.stalmate.user.view.dashboard.HomeFragment.ActivitySearch
import com.stalmate.user.view.dashboard.welcome.ActivityWelcome
import com.stalmate.user.view.photoalbum.ActivityPhotoGallery
import com.stalmate.user.view.profile.ActivityFollowersFollowingScreen
import com.stalmate.user.view.profile.ActivityOtherUserProfile
import com.stalmate.user.view.profile.ActivityProfile
import com.stalmate.user.view.profile.ActivityProfileEdit
import  com.c2m.storyviewer.screen.StoryActivity
import com.stalmate.user.modules.reels.activity.ActivityFullViewReels
import com.stalmate.user.modules.reels.activity.ActivitySongPicker
import com.stalmate.user.modules.reels.activity.ActivityVideoRecorder
import com.stalmate.user.view.dashboard.funtime.ActivityFuntimePost
import com.stalmate.user.view.dashboard.funtime.ActivityReelsByAudio
import com.stalmate.user.view.dashboard.funtime.ActivityReportUser

public class IntentHelper {

    companion object{
        fun getFragmentOTPEnter(context: Context?): Intent? {
            return Intent(context, FragmentOTPEnter::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getProfileScreen(context: Context?): Intent? {
            return Intent(context, ActivityProfile::class.java)
               .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getOtherUserProfileScreen(context: Context?): Intent? {
            return Intent(context, ActivityOtherUserProfile::class.java)
            /*   .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)*/
        }

        fun getDashboardScreen(context: Context?): Intent? {
            return Intent(context, ActivityDashboard::class.java)
               .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getCategoryCreateScreen(context: Context?): Intent? {
            return Intent(context, ActivityCategoryCreate::class.java)
               .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }


        fun getSearchScreen(context: Context?): Intent? {
            return Intent(context, ActivitySearch::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getProfileEditScreen(context: Context?): Intent? {
            return Intent(context, ActivityProfileEdit::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        fun getCreateReelsScreen(context: Context?): Intent? {
            return Intent(context, ActivityVideoRecorder::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getFollowersFollowingScreen(context: Context?): Intent? {
            return Intent(context, ActivityFollowersFollowingScreen::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getPhotoGalleryAlbumScreen(context: Context?): Intent? {
            return Intent(context, ActivityPhotoGallery::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getActivityWelcomeScreen(context: Context?): Intent? {
            return Intent(context, ActivityWelcome::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        fun getStoryActivity(context: Context?): Intent? {
            return Intent(context, StoryActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        fun getCreateFuntimePostScreen(context: Context?): Intent? {
            return Intent(context, ActivityFuntimePost::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        fun getSongPickerActivity(context: Context?): Intent? {
            return Intent(context, ActivitySongPicker::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getFullViewReelActivity(context: Context?): Intent? {
            return Intent(context, ActivityFullViewReels::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }



        fun getReelListByAudioScreen(context: Context?): Intent? {
            return Intent(context, ActivityReelsByAudio::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getReportUserScreen(context: Context?): Intent? {
            return Intent(context, ActivityReportUser::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

    }


}