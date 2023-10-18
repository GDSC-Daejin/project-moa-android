package com.example.giftmoa.BottomMenu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.giftmoa.Adapter.ShareRoomAdapter
import com.example.giftmoa.Data.SaveSharedPreference
import com.example.giftmoa.Data.ShareRoomData
import com.example.giftmoa.R
import com.example.giftmoa.databinding.FragmentShareRoomBinding
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShareRoomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShareRoomFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var sBinding : FragmentShareRoomBinding
    private var sAdapter : ShareRoomAdapter? = null
    private var shareRoomAllData = ArrayList<ShareRoomData>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private val saveSharedPreference = SaveSharedPreference()
    private var identification = ""
    private var inviteCode = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sBinding = FragmentShareRoomBinding.inflate(inflater, container, false)

        initRoomRecyclerView()

        identification = saveSharedPreference.getName(activity).toString()


        sAdapter!!.setItemClickListener(object : ShareRoomAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int, itemId: String) {

            }
        })


       sBinding.shareCreateBtn.setOnClickListener {
            val layoutInflater = LayoutInflater.from(activity)
            val view = layoutInflater.inflate(R.layout.dialog_layout, null)
            val alertDialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setView(view)
                .create()

            val dialogTitle = view.findViewById<TextView>(R.id.dialog_title)
            val dialogBtn = view.findViewById<Button>(R.id.dialog_entrance_btn)
            val dialogET = view.findViewById<EditText>(R.id.dialog_et)

            dialogTitle.text = "방 이름 설정하기"
            dialogBtn.text = "생성하기"

            dialogBtn.setOnClickListener {
                shareRoomAllData.add(ShareRoomData(dialogET.text.toString(), 1, 0, "손민성")) //identification
                println(shareRoomAllData)
                sAdapter!!.notifyDataSetChanged()
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        /*
        sBinding.shareEntranceCodeBtn.setOnClickListener {
            //사용할 곳
            val layoutInflater = LayoutInflater.from(activity)
            val view = layoutInflater.inflate(R.layout.dialog_layout, null)
            val alertDialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setView(view)
                .create()
            val dialogBtn = view.findViewById<TextView>(R.id.dialog_entrance_btn)
            val dialogET = view.findViewById<EditText>(R.id.dialog_et)

            dialogBtn.setOnClickListener {
                println(dialogET.text)
                alertDialog.dismiss()
            }

            alertDialog.show()
        }*/

        return sBinding.root
    }

    private fun initRoomRecyclerView() {
        setRoomData()
        sAdapter = ShareRoomAdapter()
        sAdapter!!.shareRoomItemData = shareRoomAllData
        sBinding.shareRoomRv.adapter = sAdapter
        sBinding.shareRoomRv.setHasFixedSize(true)
        sBinding.shareRoomRv.layoutManager = manager
    }

    private fun setRoomData() {

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ShareRoomFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShareRoomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}