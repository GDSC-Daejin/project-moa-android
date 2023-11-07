package com.example.giftmoa.Adapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.Data.GetTeamData
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.ShareRoomData
import com.example.giftmoa.Data.Team
import com.example.giftmoa.R
import com.example.giftmoa.databinding.HomeItemBinding
import com.example.giftmoa.databinding.ShareRoomItemBinding
import com.kakao.sdk.common.KakaoSdk.init
import com.kakao.sdk.common.util.SdkLogLevel
import org.w3c.dom.Text


class ShareRoomAdapter : RecyclerView.Adapter<ShareRoomAdapter.ShareViewHolder>(){
    private lateinit var binding : ShareRoomItemBinding
    var shareRoomItemData = ArrayList<Team>()
    private lateinit var context : Context
    private var inviteCode = ""

    init {
        setHasStableIds(true)
    }

    inner class ShareViewHolder(private val binding : ShareRoomItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private var shareTitle = binding.shareTitle
        private var shareNOP = binding.shareNumberOfPeople
        /*private var shareMaster = binding.shareMaster
        private var shareCouponCnt = binding.shareCouponCnt*/


        fun bind(itemData: Team, position : Int) {
            this.position = position

            if (itemData.teamImage != null) {
                shareTitle.text = itemData.teamName!!
                binding.shareTitle.setTextColor(ContextCompat.getColor(context ,R.color.moa_gray_white))

                //shareCouponCnt.text = "공유중인 쿠폰 : ${itemData.shareCouponCount.toString()}"
                shareNOP.text = "+${itemData.teamMembers!!.size.toString()}"
                binding.shareNumberOfPeople.setTextColor(ContextCompat.getColor(context ,R.color.moa_gray_white))

            } else {
                shareTitle.text = itemData.teamName
                //shareCouponCnt.text = "공유중인 쿠폰 : ${itemData.shareCouponCount.toString()}"
                shareNOP.text = "+${itemData.teamMembers!!.size.toString()}"
            }


            val requestOptions = RequestOptions()
                .centerCrop() // 또는 .fitCenter()
                .override(300, 100) // 원하는 크기로 조절

            Glide.with(context)
                .load(itemData.teamImage!!.toUri())
                .error(R.drawable.image)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("Glide", "Image load failed: ${e?.message}")
                        println(e?.message.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        println("glide")
                        return false
                    }
                })
                .into(binding.shareMainIv)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareRoomAdapter.ShareViewHolder {
        context = parent.context
        binding = ShareRoomItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        holder.bind(shareRoomItemData[holder.adapterPosition], position)

        holder.itemView.setOnClickListener {
            shareRoomItemData[holder.adapterPosition].teamCode?.let { it1 ->
                itemClickListener.onClick(it, holder.adapterPosition,
                    it1
                )
            }
        }

        /*binding.shareDeleteBtn.setOnClickListener {
            removeData(holder.adapterPosition)
        }*/

        /*binding.shareInviteBtn.setOnClickListener {
            val charSet = ('0'..'9') + ('a'..'z') + ('A'..'Z')
            val rangeRandom = List(10) {charSet.random()}.joinToString("")
            inviteCode = rangeRandom.toString()

            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.dialog_layout, null)
            val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setView(view)
                .create()

            val dialogTitle = view.findViewById<TextView>(R.id.dialog_title)
            val dialogBtn = view.findViewById<Button>(R.id.dialog_entrance_btn)
            val dialogET = view.findViewById<EditText>(R.id.dialog_et)
            val dialogCode = view.findViewById<TextView>(R.id.dialog_invite_code_tv)

            dialogCode.text = inviteCode
            dialogTitle.text = "초대하기"
            dialogBtn.text = "초대하기"

            dialogET.visibility = View.GONE
            dialogCode.visibility = View.VISIBLE

            dialogCode.setOnClickListener {
                createClipData(inviteCode)
                println(inviteCode)
            }

            dialogBtn.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
        }*/
    }

    override fun getItemCount(): Int {
        return shareRoomItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        shareRoomItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: String)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: ShareRoomAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: ShareRoomAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    //클립보드에 복사하기
    private fun createClipData(message : String) {
        val clipManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("message", message)

        clipManager.setPrimaryClip(clipData)

        Toast.makeText(context, "복사되었습니다.", Toast.LENGTH_SHORT).show()
    }
}