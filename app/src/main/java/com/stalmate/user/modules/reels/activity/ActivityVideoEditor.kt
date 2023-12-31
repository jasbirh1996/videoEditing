package com.stalmate.user.modules.reels.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.work.*
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.daasuu.imagetovideo.EncodeListener
import com.daasuu.imagetovideo.ImageToVideoConverter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack
import com.stalmate.user.intentHelper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.base.App
import com.stalmate.user.base.BaseActivity
import com.stalmate.user.databinding.ActivityPreviewVideoBinding
import com.stalmate.user.modules.reels.activity.ActivityFilter.Companion.EXTRA_SONG
import com.stalmate.user.modules.reels.activity.ActivityFilter.Companion.EXTRA_VIDEO
import com.stalmate.user.modules.reels.audioVideoTrimmer.ui.seekbar.widgets.CrystalRangeSeekbar
import com.stalmate.user.modules.reels.audioVideoTrimmer.ui.seekbar.widgets.CrystalSeekbar
import com.stalmate.user.modules.reels.audioVideoTrimmer.utils.*
import com.stalmate.user.modules.reels.photo_editing.EmojiBSFragment
import com.stalmate.user.modules.reels.photo_editing.PropertiesBSFragment
import com.stalmate.user.modules.reels.photo_editing.StickerBSFragment
import com.stalmate.user.modules.reels.photo_editing.TextEditorDialogFragment
import com.stalmate.user.modules.reels.utils.ColorSeekBar
import com.stalmate.user.modules.reels.workers.VideoTrimmerWorker
import com.stalmate.user.utilities.Common
import com.stalmate.user.utilities.ValidationHelper
import ja.burhanrashid52.photoeditor.*
import java.io.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ActivityVideoEditor : BaseActivity(), OnPhotoEditorListener,
    PropertiesBSFragment.Properties, View.OnClickListener, StickerBSFragment.StickerListener,
    EmojiBSFragment.EmojiListener {
    lateinit var binding: ActivityPreviewVideoBinding
    lateinit var mPhotoEditor: PhotoEditor
    private val globalVideoUrl = ""
    lateinit var propertiesBSFragment: PropertiesBSFragment
    lateinit var mStickerBSFragment: StickerBSFragment
    private var mediaPlayer: ExoPlayer? = null
    private var videoPath = ""
    private var audioPath = ""
    private var imagePath = ""
    private var songId = ""
    private var duration = 0
    private lateinit var exeCmd: ArrayList<String>
    val PICK_FILE = 99
    var id = 0
    private lateinit var newCommand: Array<String?>
    private lateinit var progressDialog: ProgressDialog
    override fun onClick(viewId: Int, view: View?) {

    }

    var isIMage = false

    private lateinit var mEmojiBSFragment: EmojiBSFragment
    private var DRAW_CANVASW = 0
    private var DRAW_CANVASH = 0


    var resultCallbackOfSelectedMusicTrack: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result!!.resultCode == RESULT_OK) {
                val data: Intent = result.getData()!!
                songId = data.getStringExtra(EXTRA_SONG_ID).toString()
                val name = data.getStringExtra(EXTRA_SONG_NAME)
                val audio = data.getParcelableExtra<Uri>(EXTRA_SONG_FILE)
                Log.d("klajsdasd", songId)
                Log.d("klajsdasd", audio!!.path.toString())
                /*        binding.tvMusicName.text=name
                        binding.layoutSelectedMusic.visibility=View.VISIBLE*/

                audioPath = File(audio.path!!).absolutePath
                setupDataOverExoplayer()
            }
        }


    private fun hideSystemBars() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black);
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        hideSystemBars()
        super.onCreate(savedInstanceState)

        binding = ActivityPreviewVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageViews.add(binding.imageOne)
        imageViews.add(binding.imageTwo)
        imageViews.add(binding.imageThree)
        imageViews.add(binding.imageFour)
        imageViews.add(binding.imageFive)
        imageViews.add(binding.imageSix)
        imageViews.add(binding.imageSeven)
        imageViews.add(binding.imageEight)

        isIMage = intent.getBooleanExtra("isImage", false)
        videoPath = intent.getStringExtra(EXTRA_VIDEO).toString()
        audioPath = intent.getStringExtra(EXTRA_SONG).toString()
        songId = intent.getStringExtra(EXTRA_SONG_ID).toString()
        duration = intent.getStringExtra(EXTRA_SONG_DURATION).toString().toInt()
        renderUi()
        initializePlayer()
        if (!isIMage) {
            Handler(Looper.myLooper()!!).post {
                runOnUiThread {
                    var drawable: Drawable

                    var view = View(this)
                    view.layoutParams = ViewGroup.LayoutParams(
                        displayWidth,
                        displayHeight
                    )

                    drawable = BitmapDrawable(resources, loadBitmapFromView(view))
                    drawable.setAlpha(1)
                    binding.ivImage.source.setImageDrawable(drawable)


                    mPhotoEditor = PhotoEditor.Builder(this, binding.ivImage)
                        .setPinchTextScalable(true)
                        .setClipSourceImage(true)
                        .build()
                }
            }


            binding.imgTrim.visibility = View.VISIBLE
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val metaRotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val rotation = metaRotation?.toInt() ?: 0
            if (rotation == 90 || rotation == 270) {
                DRAW_CANVASH =
                    Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                DRAW_CANVASW =
                    Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            } else {
                DRAW_CANVASW =
                    Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
                DRAW_CANVASH =
                    Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            }


        } else {
            Handler(Looper.myLooper()!!).post {
                runOnUiThread {
                    binding.videoSurface.visibility = View.GONE

                }
            }
            //  getDropboxIMGSize(Uri.parse(videoPath!!))
        }
        initViews()

    }


    fun renderUi() {
        /*            binding.ivImage.getLayoutParams().width = newCanvasWidth
            binding.ivImage.getLayoutParams().height = newCanvasHeight*/
        /*     binding.videoSurface.getLayoutParams().width = newCanvasWidth
             binding.videoSurface.getLayoutParams().height = newCanvasHeight
 */


        binding.ivImage.getLayoutParams().width = displayWidth //to be used when not in full mode
        binding.ivImage.getLayoutParams().height = displayHeight
        binding.videoSurface.getLayoutParams().width = displayWidth
        binding.videoSurface.getLayoutParams().height = displayHeight


/*        var statusBarHeight =0
        var navigationBarHeight =0
        val insets: WindowInsets =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindowManager().getCurrentWindowMetrics().getWindowInsets()
            } else {
                TODO("VERSION.SDK_INT < R")
            }
        statusBarHeight =
            insets.getInsets(WindowInsetsCompat.Type.statusBars()).top //in pixels

        navigationBarHeight =
            insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom //in pixels

        Log.d("asldkjshladww", statusBarHeight.toString())
        Log.d("asldkjshladww", displayHeight.toString())
        Log.d("asldkjshladww", navigationBarHeight.toString())
        runOnUiThread {
            binding.ivImage.getLayoutParams().width = displayWidth
            binding.ivImage.getLayoutParams().height = displayHeight+statusBarHeight+navigationBarHeight
            binding.videoSurface.getLayoutParams().width = displayWidth
            binding.videoSurface.getLayoutParams().height = displayHeight+statusBarHeight+navigationBarHeight
        }*/

    }


    fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(
            v.layoutParams.width,
            v.layoutParams.height,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }


    private fun getDropboxIMGSize(uri: Uri) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        DRAW_CANVASW =
            imageWidth
        DRAW_CANVASH =
            imageHeight

    }


    private fun initViews() {

        seekbar = findViewById(R.id.range_seek_bar)
        seekbarController = findViewById(R.id.seekbar_controller)
        seekHandler = Handler()

        progressDialog = ProgressDialog(this)
        mStickerBSFragment = StickerBSFragment()
        mStickerBSFragment.setStickerListener(this)
        propertiesBSFragment = PropertiesBSFragment()
        propertiesBSFragment.setPropertiesChangeListener(this)
        mEmojiBSFragment = EmojiBSFragment()
        runOnUiThread {
            mPhotoEditor = PhotoEditor.Builder(this, binding.ivImage)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                .setDeleteView(binding.imgDelete) //.setDefaultTextTypeface(mTextRobotoTf)

                // .setDefaultEmojiTypeface(mEmojiTypeFace)
                .build() // build photo editor sdk
        }
        mPhotoEditor.setOnPhotoEditorListener(this)
        mEmojiBSFragment?.setEmojiListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.imgDone.setOnClickListener(this)
        binding.imgDraw.setOnClickListener(this)
        binding.imgText.setOnClickListener(this)
        binding.imgUndo.setOnClickListener(this)
        binding.imgSticker.setOnClickListener(this)
        binding.ivEmoji.setOnClickListener(this)
        binding.buttonTrimDone.setOnClickListener {
            submitForTrim()
        }

        binding.ivMusic.setOnClickListener(this)
        binding.imgTrim.setOnClickListener(this)
        binding.imgDrawingDone.setOnClickListener(this)

    }

    var isTextEnabled = false

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgClose -> {

                onBackPressed()


            }
            R.id.imgDone -> saveImage()
            R.id.imgDrawingDone -> {
                binding.layoutDrawWithUndo.visibility = View.GONE
                binding.layoutTopControlls.visibility = View.VISIBLE
                binding.colorSeekBar.visibility = View.GONE
                binding.layoutTopControlls.visibility = View.VISIBLE
                binding.layoutDrawWithUndo.visibility = View.GONE
                binding.imgDone.visibility = View.VISIBLE
                mPhotoEditor.setBrushDrawingMode(false)
            }
            R.id.imgDraw -> {


                if (mPhotoEditor.brushDrawableMode == true) {
                    /*       binding.imgUndo.visibility=View.GONE
                           binding.colorSeekBar.visibility = View.GONE
                           binding.layoutTopControlls.visibility=View.VISIBLE
                           binding.layoutDrawWithUndo.visibility=View.GONE
                           mPhotoEditor.setBrushDrawingMode(false)
                           binding.imgDraw.setImageDrawable(
                               ContextCompat.getDrawable(
                                   this,
                                   R.drawable.ic_crtpost_top_sketch
                               )
                           )*/
                } else {
                    binding.imgUndo.visibility = View.VISIBLE
                    binding.colorSeekBar.visibility = View.VISIBLE
                    mPhotoEditor.setBrushDrawingMode(true)
                    binding.layoutTopControlls.visibility = View.GONE
                    binding.layoutDrawWithUndo.visibility = View.VISIBLE
                    binding.imgDone.visibility = View.GONE
                    /*   binding.imgDraw.setImageDrawable(
                           ContextCompat.getDrawable(
                               this,
                               R.drawable.ic_crtpost_top_sketch_active
                           )
                       )*/
                }


                //setDrawingMode()
                binding.colorSeekBar.setOnColorChangeListener(object :
                    ColorSeekBar.OnColorChangeListener {
                    override fun onColorChangeListener(color: Int) {
                        mPhotoEditor.brushColor = color
                    }

                })

            }

            R.id.imgText -> {


/*

                if (isTextEnabled) {
                    binding.colorSeekBar.visibility = View.GONE
                    mPhotoEditor.setBrushDrawingMode(false)
                    binding.imgText.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_crtpost_top_text
                        )
                    )
                } else {
                    binding.colorSeekBar.visibility = View.VISIBLE
                    mPhotoEditor.setBrushDrawingMode(false)


                    binding.imgText.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_crtpost_top_text_active
                        )
                    )
                }


                binding.colorSeekBar.setOnColorChangeListener(object :
                    ColorSeekBar.OnColorChangeListener {
                    override fun onColorChangeListener(color: Int) {


                    }

                })

*/


                val textEditorDialogFragment = TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditorListener {
                    override fun onDone(inputText: String?, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        mPhotoEditor.addText(inputText, styleBuilder)
                        /*    binding.txtCurrentTool.setText(R.string.label_text)*/
                    }
                })
            }


            R.id.imgUndo -> {
                //    Log.d("canvas>>", mPhotoEditor.undo().toString() + "")
                mPhotoEditor.undo()
            }

            R.id.imgSticker -> {
                if (mStickerBSFragment.isAdded) {
                    return
                }
                mStickerBSFragment.show(
                    supportFragmentManager,
                    mStickerBSFragment.tag
                )
            }

            R.id.ivEmoji -> {
                if (mEmojiBSFragment.isAdded) {
                    return
                }
                mEmojiBSFragment!!.show(
                    supportFragmentManager,
                    mEmojiBSFragment!!.tag
                )
            }

            R.id.ivMusic -> {
                resultCallbackOfSelectedMusicTrack.launch(IntentHelper.getSongPickerActivity(this))
            }

            R.id.imgTrim -> {
                if (binding.layoutTrim.visibility == View.VISIBLE) {
                    binding.layoutTrim.visibility = View.GONE
                    binding.imgDone.visibility = View.VISIBLE
                } else {
                    binding.layoutTrim.visibility = View.VISIBLE
                    binding.imgDone.visibility = View.GONE
                }
            }

        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
/*        var commonConfirmationDialog = CommonConfirmationDialog(
            this,
            "Save as Draft",
            "Drafts let you save your edits, so you can come back later.",
            "Yes",
            "Delete Video",
            object : CommonConfirmationDialog.Callback {
                override fun onDialogResult(isPermissionGranted: Boolean) {
                    finish()
                }
            })
        commonConfirmationDialog.show()*/
    }


    private fun setDrawingMode() {
        if (mPhotoEditor.brushDrawableMode == true) {
            mPhotoEditor.setBrushDrawingMode(false)
            binding.imgDraw.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_crtpost_top_sketch
                )
            )
        } else {
            mPhotoEditor.setBrushDrawingMode(true)


            binding.imgDraw.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_crtpost_top_sketch_active
                )
            )

            propertiesBSFragment.show(getSupportFragmentManager(), propertiesBSFragment.getTag())
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveImage() {

        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                .toString() + File.separator + ""
                    + System.currentTimeMillis() + ".png"
        )

        showLoader()
        try {
            file.createNewFile()
            val saveSettings: SaveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(false)
                .setTransparencyEnabled(false)
                .build()
            mPhotoEditor.saveAsFile(
                file.absolutePath,
                saveSettings,
                object : PhotoEditor.OnSaveListener {
                    override
                    fun onSuccess(@NonNull imagePath: String) {
                        //  dismissLoader()
                        this@ActivityVideoEditor.imagePath = imagePath
                        Log.d("imagePath>>", imagePath)
                        Log.d("imagePath2>>", Uri.fromFile(File(imagePath)).toString())
                        binding.ivImage.source.setImageURI(Uri.fromFile(File(imagePath)))
                        /*        Toast.makeText(
                                    this@ActivityVideoEditor,
                                    "Saved successfully...",
                                    Toast.LENGTH_SHORT
                                ).show()*/

                        if (isIMage) {
                            imageToVideo(imagePath)
                            //  convertImageToVideo(imagePath)
                        } else {
                            mergeAudioVideo(videoPath)
                            /*  applayWaterMark(File(videoPath))*/
                        }


                        //  saveVideoToInternalStorage()
                    }

                    override fun onFailure(exception: Exception) {
                        dismissLoader()
                        Toast.makeText(
                            this@ActivityVideoEditor,
                            "Saving Failed...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                })
        } catch (e: IOException) {
            dismissLoader()
            e.printStackTrace()
        }
    }

    private fun applayWaterMark(beforeWatermarkAddedFile: File) {
        try {
            addToWatermark(beforeWatermarkAddedFile)
        } catch (e: Exception) {
            Log.d("lkajsdlasd", e.toString())
            e.printStackTrace()
        }
    }

    override
    fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor.setBrushDrawingMode(false)
        binding.imgDraw.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trasp))

        mPhotoEditor.addImage(bitmap)
    }


    private val displayWidth: Int
        private get() {
            val displayMetrics = DisplayMetrics()
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }
    private val displayHeight: Int
        private get() {
            val displayMetrics = DisplayMetrics()
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }

    override
    fun onColorChanged(colorCode: Int) {


        mPhotoEditor.brushColor = colorCode

    }

    override
    fun onOpacityChanged(opacity: Int) {


    }

    override
    fun onBrushSizeChanged(brushSize: Int) {
    }

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        val textEditorDialogFragment =
            TextEditorDialogFragment.show(this, text.toString(), colorCode)
        textEditorDialogFragment.setOnTextEditorListener(object :
            TextEditorDialogFragment.TextEditorListener {
            override fun onDone(inputText: String?, colorCode: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
                if (rootView != null) {
                    mPhotoEditor.editText(rootView, inputText, styleBuilder)
                }

                /*      binding.txtCurrentTool.setText(R.string.label_text)*/
            }
        })
    }


    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

    }

    override fun onStartViewChangeListener(viewType: ViewType?) {

    }

    override fun onStopViewChangeListener(viewType: ViewType?) {

    }

    override fun onTouchSourceImage(event: MotionEvent?) {

    }


    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor.addEmoji(emojiUnicode)
        // binding.txtCurrentTool.setText(R.string.label_emoji)
    }


    fun addToWatermark(beforeWatermarkAddedFile: File) {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        ffmpegWatermark(beforeWatermarkAddedFile.absolutePath, outputPath, imagePath)
    }


    private fun saveVideoToInternalStorage(path: String) {
        try {
            val currentFile: File = File(path)
            val newfile = File(Common.getFilePath(this, Common.VIDEO))
            if (currentFile.exists()) {
                Handler(Looper.getMainLooper()).post {
                    val inputStream: InputStream = FileInputStream(currentFile)
                    val outputStream: OutputStream = FileOutputStream(newfile)
                    val buf = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buf).also { len = it } > 0) {
                        outputStream.write(buf, 0, len)
                    }
                    outputStream.flush()
                    inputStream.close()
                    outputStream.close()
                }
                startActivity(
                    IntentHelper.getCreateFuntimePostScreen(this)!!
                        .putExtra(EXTRA_VIDEO, newfile.absolutePath).putExtra(EXTRA_SONG_ID, songId)
                )
            } else {
                Toast.makeText(
                    applicationContext,
                    "Video has failed for saving!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun releasePlayer() {
        mediaPlayer!!.release()
    }

    public override fun onStart() {
        super.onStart()
        setupDataOverExoplayer()
    }


    fun initializePlayer() {
        mediaPlayer = ExoPlayer.Builder(this@ActivityVideoEditor).build()
        binding.videoSurface.player = mediaPlayer
        binding.videoSurface.hideController()
        binding.videoSurface.useController = false
        mediaPlayer!!.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        mediaPlayer!!.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                /*   imagePlayPause.visibility = if (playWhenReady) View.GONE else View.VISIBLE*/
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> {
                        /*      imagePlayPause.visibility = View.VISIBLE*/
                        isVideoEnded = true
                    }
                    Player.STATE_READY -> {
                        isVideoEnded = false
                        /*         imagePlayPause.visibility = View.GONE*/
                        startProgress() //use this when start audio trimmer

                    }
                    else -> {}
                }
            }
        })

    }

    override fun onPause() {
        mediaPlayer!!.pause()
        super.onPause()

    }

    fun setupDataOverExoplayer() {

        var videoSource: MediaSource? = null
        var audioSource: MediaSource? = null
        var mergedSource: MediaSource? = null
        if (!isIMage) {
            videoSource =
                ProgressiveMediaSource.Factory(FileDataSource.Factory()).createMediaSource(
                    MediaItem.fromUri(Uri.parse(videoPath))
                )
            if (!ValidationHelper.isNull(audioPath)) {
                audioSource =
                    ProgressiveMediaSource.Factory(FileDataSource.Factory()).createMediaSource(
                        MediaItem.fromUri(Uri.parse(audioPath))
                    )
                mergedSource = MergingMediaSource(videoSource!!, audioSource);
                mediaPlayer!!.setMediaSource(mergedSource)

            } else {


                mediaPlayer!!.setMediaSource(videoSource)

            }
        } else {


            if (!ValidationHelper.isNull(audioPath)) {
                audioSource =
                    ProgressiveMediaSource.Factory(FileDataSource.Factory()).createMediaSource(
                        MediaItem.fromUri(Uri.parse(audioPath))
                    )
                mediaPlayer!!.setMediaSource(audioSource)
            }

            Handler(Looper.myLooper()!!).post {
                runOnUiThread {
                    Glide.with(this).load(Drawable.createFromPath(videoPath)).centerCrop()
                        .into(binding.ivImage.source)
                    binding.ivImage.source.scaleType = ImageView.ScaleType.FIT_XY
                }
            }


        }


        mediaPlayer!!.playWhenReady = true
        mediaPlayer!!.prepare()
        setDataInView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) mediaPlayer!!.release()
        deleteFile("temp_file")
        stopRepeatingTask()
    }


    lateinit var imageToVideo: ImageToVideoConverter
    private fun convertImageToVideo(imagePath: String) {
        //int height = getInputData().getInt(ICON,0);
        // int width = getInputData().getInt(ICON,0);

        val outputPath = Common.getFilePath(this, Common.VIDEO)
        imageToVideo = ImageToVideoConverter(
            outputPath = outputPath,
            inputImagePath = imagePath,
            size = Size(528, 1072),
            duration = TimeUnit.SECONDS.toMicros(10),
            listener = object : EncodeListener {
                override fun onProgress(progress: Float) {
                    Log.d("progress", "progress = $progress")
                    runOnUiThread {

                    }
                }

                override fun onCompleted() {
                    runOnUiThread {
                        mergeAudioVideo(outputPath)
                    }
                }

                override fun onFailed(exception: Exception) {

                }
            }
        )
        imageToVideo?.start()


    }

    private fun mergeAudioVideo(filePath: String) {
        if (!ValidationHelper.isNull(audioPath)) {
            val output = File(cacheDir, UUID.randomUUID().toString() + ".mp4")

            val asyncTask =
                FFmpegAsyncTask("-i $filePath -i $audioPath -map 0:v -map 1:a -t $duration -crf 23 -preset ultrafast -vcodec libx264 -c:a aac $output",
                    object : FFmpegAsyncTask.OnTaskCompleted {
                        override fun onTaskCompleted(isSuccess: Boolean) {
                            runOnUiThread {
                                applayWaterMark(output)
                            }

                        }
                    })
            asyncTask.execute()


        } else {
            applayWaterMark(File(filePath))
        }
    }


    private fun getCroppedTrack(track: Track, startTimeMs: Int, endTimeMs: Int): CroppedTrack? {
        var currentSample: Long = 0
        var currentTime = 0.0
        var startSample: Long = -1
        var endSample: Long = -1
        val startTime = (startTimeMs / 1000).toDouble()
        val endTime = (endTimeMs / 1000).toDouble()
        for (i in 0 until track.sampleDurations.size) {
            if (currentTime <= startTime) {
                // current sample is still before the new starttime
                startSample = currentSample
            }
            endSample = if (currentTime <= endTime) {
                // current sample is after the new start time and still before the new endtime
                currentSample
            } else {
                // current sample is after the end of the cropped video
                break
            }
            currentTime += track.sampleDurations.get(i).toDouble() / track.trackMetaData
                .timescale.toDouble()
            currentSample++
        }
        return CroppedTrack(track, startSample, endSample)
    }


/*    private lateinit var imagePlayPause: ImageView*/

    private var imageViews = ArrayList<ImageView>()

    private var totalDuration: Long = 0

    private lateinit var dialog: Dialog


/*    private lateinit var txtStartDuration: TextView
    private  lateinit var txtEndDuration:TextView*/

    private lateinit var seekbar: CrystalRangeSeekbar

    private var lastMinValue: Long = 0

    private var lastMaxValue: Long = 0

    private var menuDone: MenuItem? = null

    private var seekbarController: CrystalSeekbar? = null

    private var isValidVideo = true
    private var isVideoEnded: kotlin.Boolean = false

    private var seekHandler: Handler? = null

    private lateinit var bundle: Bundle


    private var currentDuration: Long = 0
    private var lastClickedTime: kotlin.Long = 0
    var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                Log.d("aksdasda", "ioasjdsa")
                currentDuration = mediaPlayer!!.currentPosition / 1000
                if (!mediaPlayer!!.playWhenReady) return
                if (currentDuration <= lastMaxValue) seekbarController!!.setMinStartValue(
                    currentDuration.toInt().toFloat()
                ).apply() else mediaPlayer!!.playWhenReady = false
            } finally {
                seekHandler!!.postDelayed(this, 1000)
            }
        }
    }
    private var trimType = 0
    private var fixedGap: Long = 0
    private var minGap: kotlin.Long = 0
    private var minFromGap: kotlin.Long = 0
    private var maxToGap: kotlin.Long = 0
    private var hidePlayerSeek =
        false
    private var progressView: CustomProgressView? = null


    private fun setUpSeekBar() {
        seekbar.visibility = View.VISIBLE
        /*       txtStartDuration.setVisibility(View.VISIBLE)
               txtEndDuration.setVisibility(View.VISIBLE)*/
        seekbarController!!.setMaxValue(totalDuration.toFloat()).apply()
        seekbar.setMaxValue(totalDuration.toFloat()).apply()
        seekbar.setMaxStartValue(totalDuration.toFloat()).apply()
        lastMaxValue = if (trimType == 1) {
            seekbar.setFixGap(fixedGap.toFloat()).apply()
            totalDuration
        } else if (trimType == 2) {
            seekbar.setMaxStartValue(minGap.toFloat())
            seekbar.setGap(minGap.toFloat()).apply()
            totalDuration
        } else if (trimType == 3) {
            seekbar.setMaxStartValue(maxToGap.toFloat())
            seekbar.setGap(minFromGap.toFloat()).apply()
            maxToGap
        } else {
            seekbar.setGap(2F).apply()
            totalDuration
        }
        if (hidePlayerSeek) seekbarController!!.visibility = View.GONE
        seekbar.setOnRangeSeekbarFinalValueListener { minValue, maxValue ->
            if (!hidePlayerSeek) seekbarController!!.visibility = View.VISIBLE
        }
        seekbar.setOnRangeSeekbarChangeListener { minValue, maxValue ->
            val minVal = minValue as Long
            val maxVal = maxValue as Long
            if (lastMinValue != minVal) {
                seekTo(minValue)
                if (!hidePlayerSeek) seekbarController!!.visibility = View.INVISIBLE
            }
            lastMinValue = minVal
            lastMaxValue = maxVal
            seekbar.setLeftThumbBitmap(
                createDrawableFromView(
                    lastMinValue,
                    R.drawable.switch_button_thumb
                )
            )
            seekbar.setRightThumbBitmap(
                createDrawableFromView(
                    lastMaxValue,
                    R.drawable.switch_button_thumb
                )
            )

            Log.d("akjsdasdoo", lastMinValue.toString())
            Log.d("akjsdasdoo", lastMaxValue.toString())

/*            txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal))
            txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal))*/
            if (trimType == 3) setDoneColor(minVal, maxVal)
        }
        seekbarController!!.setOnSeekbarFinalValueListener { value ->
            val value1 = value as Long
            if (value1 in (lastMinValue + 1) until lastMaxValue) {
                seekTo(value1)
                return@setOnSeekbarFinalValueListener
            }
            if (value1 > lastMaxValue) seekbarController!!.setMinStartValue(
                lastMaxValue.toInt().toFloat()
            ).apply() else if (value1 < lastMinValue) {
                seekbarController!!.setMinStartValue(lastMinValue.toInt().toFloat()).apply()
                if (mediaPlayer!!.playWhenReady) seekTo(lastMinValue)
            }
        }
    }


    fun createDrawableFromView(progress: Long, drawable: Int): Bitmap? {
        val inflatedFrame: View = (App.getInstance()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.view_custom_thumb,
            null
        )
        /*
            val ivStar: ImageView = inflatedFrame.findViewById(R.id.ivIcon) as ImageView

              ivStar.setImageDrawable(
                  ContextCompat.getDrawable(
                      App.getInstance(),
                      drawable
                  )
              )
      */
        val tvValue: TextView = inflatedFrame.findViewById(R.id.tvValue) as TextView
        tvValue.setText(progress.toString() + "s")
        val frameLayout: FrameLayout = inflatedFrame.findViewById(R.id.screen) as FrameLayout


        frameLayout.setDrawingCacheEnabled(true)
        frameLayout.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        frameLayout.layout(0, 0, frameLayout.getMeasuredWidth(), frameLayout.getMeasuredHeight())
        frameLayout.buildDrawingCache(true)
        return frameLayout.getDrawingCache()
    }


    private fun seekTo(sec: Long) {
        if (mediaPlayer != null) mediaPlayer!!.seekTo(sec * 1000)
    }

    private fun setDoneColor(minVal: Long, maxVal: Long) {
        try {
            if (menuDone == null) return
            //changed value is less than maxDuration
            if (maxVal - minVal <= maxToGap) {
                menuDone!!.icon!!.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(this, R.color.white),
                        PorterDuff.Mode.SRC_IN
                    )
                isValidVideo = true
            } else {
                menuDone!!.icon!!.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(this, R.color.black),
                        PorterDuff.Mode.SRC_IN
                    )
                isValidVideo = false
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun setDataInView() {
        try {
            val fileUriRunnable = Runnable {
                runOnUiThread {
                    totalDuration = TrimmerUtils.getDuration(this, Uri.parse(videoPath))

                    // imagePlayPause.setOnClickListener { v: View? -> onVideoClicked() }
                    /*                 Objects.requireNonNull<View>(playerView.getVideoSurfaceView())
                                         .setOnClickListener { v: View? -> onVideoClicked() }*/

                    loadThumbnails()
                    setUpSeekBar()
                }
            }
            Executors.newSingleThreadExecutor().execute(fileUriRunnable)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /*
     *  loading thumbnails
     * */
    private fun loadThumbnails() {
        try {
            val diff = totalDuration / 8
            var sec = 1
            for (img in imageViews) {

                Log.d("klasgfiaf", "poapsdpwof")
                /*      val retriever = MediaMetadataRetriever()
                      retriever.setDataSource(videoFile.getAbsolutePath())
                      val bitmap = retriever.getFrameAtTime(
                          totalDuration * 1000,
                          MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                      )

                      */


                val interval = diff * sec * 1000000
                val options = RequestOptions().frame(interval)
                Glide.with(this)
                    .load(bundle.getString(TrimVideo.TRIM_VIDEO_URI))
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(img)
                if (sec < totalDuration) sec++
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun startProgress() {
        updateSeekbar.run()
    }


    fun stopRepeatingTask() {
        seekHandler!!.removeCallbacks(updateSeekbar)
    }

    private fun submitForTrim() {
        binding.imgDone.visibility = View.VISIBLE
        val wm = WorkManager.getInstance(this)
        val trimmed = File(cacheDir, UUID.randomUUID().toString())
        val data = Data.Builder()
            .putString(VideoTrimmerWorker.KEY_INPUT, videoPath)
            .putString(VideoTrimmerWorker.KEY_OUTPUT, trimmed.absolutePath)
            .putLong(VideoTrimmerWorker.KEY_START, lastMinValue * 1000)
            .putLong(VideoTrimmerWorker.KEY_END, lastMaxValue * 1000)
            .build()
        val request = OneTimeWorkRequest.Builder(VideoTrimmerWorker::class.java)
            .setInputData(data)
            .build()
        wm.enqueue(request)
        wm.getWorkInfoByIdLiveData(request.id)
            .observe(this) { info: WorkInfo ->
                val ended = (info.state == WorkInfo.State.CANCELLED
                        || info.state == WorkInfo.State.FAILED)
                if (info.state == WorkInfo.State.SUCCEEDED) {
                    videoPath = trimmed.absolutePath
                    binding.layoutTrim.visibility = View.GONE
                    setupDataOverExoplayer()
                } else if (ended) {

                }
            }
    }

    private fun imageToVideo(imagePath: String) {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        var height = 0
        var width = 0
        if (displayWidth % 2 == 0) {
            width = displayWidth
        } else {
            width = displayWidth - 1
        }

        if (displayHeight % 2 == 0) {
            height = displayHeight
        } else {
            height = displayHeight - 1
        }

        val asyncTask =
            FFmpegAsyncTask("-loop 1  -framerate 3 -i $imagePath -t $duration -pix_fmt yuv420p  -crf 30 -preset ultrafast -vcodec libx264 -c:a aac -vf scale=$width:$height  $outputPath",
                object : FFmpegAsyncTask.OnTaskCompleted {
                    override fun onTaskCompleted(isSuccess: Boolean) {
                        runOnUiThread {
                            mergeAudioVideo(outputPath)
                        }
                    }
                })
        asyncTask.execute()
    }


    fun ffmpegWatermark(input: String, output: String, image: String) {
        val asyncTask =
            FFmpegAsyncTask("-i $input -i $image -filter_complex [1][0]scale2ref=w=oh*mdar:h=ih*1.1[logo][video];[video][logo]overlay=0:0 -crf 23 -preset ultrafast -vcodec libx264 -c:a aac $output",
                object : FFmpegAsyncTask.OnTaskCompleted {
                    override fun onTaskCompleted(isSuccess: Boolean) {
                        runOnUiThread {
                            dismissLoader()
                            saveVideoToInternalStorage(output)
                        }
                    }
                })
        asyncTask.execute()
    }

    fun generateCreateVideoWithPipesScript(
        image3Pipe: String,
        videoFilePath: String
    ): String? {
        return "-i " + image3Pipe + " " +
                "-filter_complex \"" +
                "[0:v]loop=loop=-1:size=1:start=0,setpts=PTS-STARTPTS,scale=w=\'if(gte(iw/ih,640/427),min(iw,640),-1)\':h=\'if(gte(iw/ih,640/427),-1,min(ih,427))\',scale=trunc(iw/2)*2:trunc(ih/2)*2,setsar=sar=1/1,split=2[stream1out1][stream1out2];" +
                "[1:v]loop=loop=-1:size=1:start=0,setpts=PTS-STARTPTS,scale=w=\'if(gte(iw/ih,640/427),min(iw,640),-1)\':h=\'if(gte(iw/ih,640/427),-1,min(ih,427))\',scale=trunc(iw/2)*2:trunc(ih/2)*2,setsar=sar=1/1,split=2[stream2out1][stream2out2];" +
                "[2:v]loop=loop=-1:size=1:start=0,setpts=PTS-STARTPTS,scale=w=\'if(gte(iw/ih,640/427),min(iw,640),-1)\':h=\'if(gte(iw/ih,640/427),-1,min(ih,427))\',scale=trunc(iw/2)*2:trunc(ih/2)*2,setsar=sar=1/1,split=2[stream3out1][stream3out2];" +
                "[stream1out1]pad=width=640:height=427:x=(640-iw)/2:y=(427-ih)/2:color=#00000000,trim=duration=3,select=lte(n\\,90)[stream1overlaid];" +
                "[stream1out2]pad=width=640:height=427:x=(640-iw)/2:y=(427-ih)/2:color=#00000000,trim=duration=1,select=lte(n\\,30)[stream1ending];" +
                "[stream2out1]pad=width=640:height=427:x=(640-iw)/2:y=(427-ih)/2:color=#00000000,trim=duration=2,select=lte(n\\,60)[stream2overlaid];" +
                "[stream2out2]pad=width=640:height=427:x=(640-iw)/2:y=(427-ih)/2:color=#00000000,trim=duration=1,select=lte(n\\,30),split=2[stream2starting][stream2ending];" +
                "[stream3out1]pad=width=640:height=427:x=(640-iw)/2:y=(427-ih)/2:color=#00000000,trim=duration=2,select=lte(n\\,60)[stream3overlaid];" +
                "[stream3out2]pad=width=640:height=427:x=(640-iw)/2:y=(427-ih)/2:color=#00000000,trim=duration=1,select=lte(n\\,30)[stream3starting];" +
                "[stream2starting][stream1ending]blend=all_expr=\'if(gte(X,(W/2)*T/1)*lte(X,W-(W/2)*T/1),B,A)\':shortest=1[stream2blended];" +
                "[stream3starting][stream2ending]blend=all_expr=\'if(gte(X,(W/2)*T/1)*lte(X,W-(W/2)*T/1),B,A)\':shortest=1[stream3blended];" +
                "[stream1overlaid][stream2blended][stream2overlaid][stream3blended][stream3overlaid]concat=n=5:v=1:a=0,scale=w=640:h=424,format=yuv420p[video]\"" +
                " -map [video] -vsync 2 -async 1 -c:v mpeg4 -r 30 " + videoFilePath
    }


    private class FFmpegAsyncTask(var command: String, var callback: OnTaskCompleted) :
        AsyncTask<Void?, Void?, Void?>() {
        protected override fun onPreExecute() {
            super.onPreExecute()

        }

        protected override fun doInBackground(vararg nc: Void?): Void? {

            FFmpeg.execute(command);
            return null
        }

        protected override fun onPostExecute(v: Void?) {
            callback.onTaskCompleted(true)
            super.onPostExecute(v)
        }


        public interface OnTaskCompleted {
            fun onTaskCompleted(isSuccess: Boolean);
        }
    }

}