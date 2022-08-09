package com.user.vaibhavmodules.reels.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.simform.videooperations.*
import com.stalmate.user.R
import com.stalmate.user.base.BaseActivity
import com.stalmate.user.databinding.ActivityPreviewVideoBinding
import com.stalmate.user.modules.reels.photo_editing.PropertiesBSFragment
import com.stalmate.user.modules.reels.photo_editing.StickerBSFragment
import com.stalmate.user.modules.reels.photo_editing.TextEditorDialogFragment

import com.user.vaibhavmodules.reels.photo_editing.EmojiBSFragment

import ja.burhanrashid52.photoeditor.*
import ja.burhanrashid52.photoeditor.Utils.getScaledDimension
import java.io.File
import java.io.IOException

class ActivityVideoEditor() : BaseActivity(), OnPhotoEditorListener,
    PropertiesBSFragment.Properties, View.OnClickListener, StickerBSFragment.StickerListener,
    EmojiBSFragment.EmojiListener {
    lateinit var binding: ActivityPreviewVideoBinding
    lateinit var mPhotoEditor: PhotoEditor
    private val globalVideoUrl = ""
    lateinit var propertiesBSFragment: PropertiesBSFragment
    lateinit var mStickerBSFragment: StickerBSFragment
    private var mediaPlayer: MediaPlayer? = null
    private var videoPath = ""
    private var imagePath = ""
    private var exeCmd: ArrayList<String>? = null
    lateinit var fFmpeg: FFmpeg
    private lateinit var newCommand: Array<String?>
    private var progressDialog: ProgressDialog? = null
    override fun onClick(viewId: Int, view: View?) {

    }

    private var originalDisplayWidth = 0
    private var originalDisplayHeight = 0
    private var newCanvasWidth = 0
    private var newCanvasHeight = 0
    private var mEmojiBSFragment: EmojiBSFragment? = null
    private var DRAW_CANVASW = 0
    private var DRAW_CANVASH = 0
    private val onCompletionListener: OnCompletionListener =
        OnCompletionListener { mediaPlayer -> mediaPlayer.start() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding= ActivityPreviewVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        //        Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
//        Glide.with(this).load(getIntent().getStringExtra("DATA")).into(binding.ivImage.getSource());
       Glide.with(this).load(R.drawable.trans).centerCrop().into(binding.ivImage.source)
        videoPath = intent.getStringExtra("DATA").toString()
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        val metaRotation =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
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
        setCanvasAspectRatio()
        binding.videoSurface.getLayoutParams().width = newCanvasWidth
        binding.videoSurface.getLayoutParams().height = newCanvasHeight
        binding.ivImage.getLayoutParams().width = newCanvasWidth
        binding.ivImage.getLayoutParams().height = newCanvasHeight
        Log.d(
            ">>",
            "width>> " + newCanvasWidth + "height>> " + newCanvasHeight + " rotation >> " + rotation
        )

    }

    private fun initViews() {
       fFmpeg = FFmpeg.getInstance(this)
        progressDialog = ProgressDialog(this)
        mStickerBSFragment = StickerBSFragment()
        mStickerBSFragment.setStickerListener(this)
        propertiesBSFragment = PropertiesBSFragment()
        propertiesBSFragment.setPropertiesChangeListener(this)
        mEmojiBSFragment = EmojiBSFragment()
        mPhotoEditor = PhotoEditor.Builder(this, binding.ivImage)
            .setPinchTextScalable(true) // set flag to make text scalable when pinch
            .setDeleteView(binding.imgDelete) //.setDefaultTextTypeface(mTextRobotoTf)
           // .setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk
        mPhotoEditor.setOnPhotoEditorListener(this)
        mEmojiBSFragment?.setEmojiListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.imgDone.setOnClickListener(this)
        binding.imgDraw.setOnClickListener(this)
        binding.imgText.setOnClickListener(this)
        binding.imgUndo.setOnClickListener(this)
        binding.imgSticker.setOnClickListener(this)
        binding.ivEmoji.setOnClickListener(this)
        binding.videoSurface.setSurfaceTextureListener(object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                i: Int,
                i1: Int
            ) {
//                activityHomeBinding.videoSurface.getLayoutParams().height=640;
//                activityHomeBinding.videoSurface.getLayoutParams().width=720;
                val surface = Surface(surfaceTexture)
                try {
                    mediaPlayer = MediaPlayer()
                    //                    mediaPlayer.setDataSource("http://daily3gp.com/vids/747.3gp");
                    Log.d("VideoPath>>", videoPath)
                    mediaPlayer!!.setDataSource(videoPath)
                    mediaPlayer!!.setSurface(surface)
                    mediaPlayer!!.prepare()
                    mediaPlayer!!.setOnCompletionListener(onCompletionListener)
                    mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer!!.start()
                } catch (e: IllegalArgumentException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: SecurityException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IllegalStateException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                i: Int,
                i1: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
        })
        exeCmd = ArrayList()
    try {
            fFmpeg.loadBinary(object : FFmpegLoadBinaryResponseHandler {
                override
                fun onFailure() {
                    Log.d("binaryLoad", "onFailure")
                }
                override
                fun onSuccess() {
                    Log.d("binaryLoad", "onSuccess")
                }
                override
                fun onStart() {
                    Log.d("binaryLoad", "onStart")
                }
                override
                fun onFinish() {
                    Log.d("binaryLoad", "onFinish")
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }
    }

   fun executeCommand(command: Array<String?>?, absolutePath: String?) {
       Log.e("asgjhdasd","alisjdlad")
       fFmpeg.execute(command, object : FFmpegExecuteResponseHandler {
           override
           fun onSuccess(s: String) {
               Log.d("CommandExecute", "onSuccess  $s")
               Toast.makeText(getApplicationContext(), "Sucess", Toast.LENGTH_SHORT).show()
               /*      val i = Intent(this@ActivityVideoEditor, ActivityVideoEditor::class.java)
                     i.putExtra("DATA", absolutePath)
                     startActivity(i)*/
           }
           override
           fun onProgress(s: String) {
               progressDialog!!.setMessage(s)
               Log.d("CommandExecute", "onProgress  $s")
           }
           override
           fun onFailure(s: String) {
               Log.d("CommandExecute", "onFailure  $s")
               progressDialog!!.hide()
           }
           override
           fun onStart() {
               progressDialog!!.setTitle("Preccesing")
               progressDialog!!.setMessage("Starting")
               progressDialog!!.show()
           }
           override
           fun onFinish() {
               progressDialog!!.hide()
           }
       })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgClose -> onBackPressed()
            R.id.imgDone -> saveImage()
            R.id.imgDraw -> setDrawingMode()
            R.id.imgText -> {    val textEditorDialogFragment = TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object : TextEditorDialogFragment.TextEditorListener {
                    override fun onDone(inputText: String?, colorCode: Int) {
                       val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        mPhotoEditor.addText(inputText, styleBuilder)
                  /*    binding.txtCurrentTool.setText(R.string.label_text)*/
                    }
                })
            }
            R.id.imgUndo -> {
                Log.d("canvas>>", mPhotoEditor.undo().toString() + "")
                mPhotoEditor.clearAllViews()
            }
            R.id.imgSticker -> mStickerBSFragment.show(
                supportFragmentManager,
                mStickerBSFragment.tag
            )

            R.id.ivEmoji -> mEmojiBSFragment!!.show(
                supportFragmentManager,
                mEmojiBSFragment!!.tag
            )
        }
    }

    private fun setCanvasAspectRatio() {
        originalDisplayHeight = displayHeight
        originalDisplayWidth = displayWidth
        val displayDiamenion: DimensionData = getScaledDimension(
            DimensionData(
                DRAW_CANVASW,
                DRAW_CANVASH
            ),
            DimensionData(originalDisplayWidth, originalDisplayHeight)
        )
        newCanvasWidth = displayDiamenion.width
        newCanvasHeight = displayDiamenion.height
    }

    private fun setDrawingMode() {
        if (mPhotoEditor.brushDrawableMode == true) {
            mPhotoEditor.setBrushDrawingMode(false)
            binding.imgDraw.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trasp))
        } else {
            mPhotoEditor.setBrushDrawingMode(true)
            binding.imgDraw.setBackgroundColor(ContextCompat.getColor(this, R.color.progress_primary))
            propertiesBSFragment.show(getSupportFragmentManager(), propertiesBSFragment.getTag())
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveImage() {
        showLoader()
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                .toString() + File.separator + ""
                    + System.currentTimeMillis() + ".png"
        )


        try {
            file.createNewFile()
            val saveSettings: SaveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(false)
                .build()
            mPhotoEditor.saveAsFile(file.absolutePath, saveSettings, object : PhotoEditor.OnSaveListener {
                override
                fun onSuccess(@NonNull imagePath: String) {
                    dismissLoader()
                    this@ActivityVideoEditor.imagePath = imagePath
                    Log.d("imagePath>>", imagePath)
                    Log.d("imagePath2>>", Uri.fromFile(File(imagePath)).toString())
                    binding.ivImage.source.setImageURI(Uri.fromFile(File(imagePath)))
                    Toast.makeText(
                        this@ActivityVideoEditor,
                        "Saved successfully...",
                        Toast.LENGTH_SHORT
                    ).show()
                    applayWaterMark()
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

    private fun applayWaterMark() {

        try {

            addWaterMarkProcess()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
        override
    fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor.setBrushDrawingMode(false)
        binding.imgDraw.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trasp))
        mPhotoEditor.addImage(bitmap)
            binding.txtCurrentTool.setText(R.string.label_sticker)
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
        mPhotoEditor.brushColor=colorCode
        binding.txtCurrentTool.setText(R.string.label_brush)
    }
    override
    fun onOpacityChanged(opacity: Int) {
        binding.txtCurrentTool.setText(R.string.label_brush)

    }
    override
    fun onBrushSizeChanged(brushSize: Int) {}

    companion object {
        private val TAG = ActivityVideoEditor::class.java.simpleName
        private val CAMERA_REQUEST = 52
        private val PICK_REQUEST = 53
    }



    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        val textEditorDialogFragment = TextEditorDialogFragment.show(this, text.toString(), colorCode)
        textEditorDialogFragment.setOnTextEditorListener (object : TextEditorDialogFragment.TextEditorListener {
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




    val ffmpegQueryExtension = FFmpegQueryExtension()
    private fun addWaterMarkProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        val xPos = 0f
        val yPos = 0f
        val query = ffmpegQueryExtension.addVideoWaterMark(videoPath, imagePath, xPos, yPos, outputPath)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {

            }

            override fun success() {

            }

            override fun cancel() {

            }

            override fun failed() {

            }
        })
    }




}