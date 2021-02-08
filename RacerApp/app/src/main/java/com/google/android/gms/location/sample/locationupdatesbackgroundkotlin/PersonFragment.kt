package com.google.android.gms.location.sample.locationupdatesbackgroundkotlin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.data.SocketPacket
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.dummy.DummyContent
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_person_list.view.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.SocketException
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.floor

/**
 * A fragment representing a list of Items.
 */
class PersonFragment : Fragment() {


    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        val view = inflater.inflate(R.layout.fragment_person_list, container, false)

        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MyPersonRecyclerViewAdapter(DummyContent.ITEMS)
            }
        }

        val mainThread = Handler(Looper.getMainLooper())

        Executors.newSingleThreadExecutor().execute {

            while(true){
                try {
                    val socket = Socket(IP_ADDRESS, PORT)

                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    val json = Gson().toJson(SocketPacket(operation = O_GET_LEADERBOARD))
                    socket.outputStream.write(json.toByteArray())
                    socket.outputStream.write("\n".toByteArray())
                    socket.outputStream.flush()
                    var inputLine: String
                    while (reader.readLine().also { inputLine = it } != null) {
                        val packet = Gson().fromJson(
                            inputLine,
                            SocketPacket::class.java
                        )
                        if (packet?.leaderboard == null)
                            break

                        var items: MutableList<DummyContent.DummyItem> = ArrayList()
                        for (userDistance in packet.leaderboard!!) {
                            items.add(
                                DummyContent.createDummyItem(
                                    items.size + 1,
                                    userDistance.username!!,
                                    (floor(
                                        userDistance.distance!!
                                    ) / 1000).toString() + " km"
                                )
                            )
                        }
                        mainThread.post( Runnable () {
                            view.list.adapter = MyPersonRecyclerViewAdapter(items)
                        });
                        Log.i("Get Leaderboard: ", items.toString())

                    }
                    socket.close()
                } catch (e: SocketException) {
                    Log.e("SOCKET EXCEPTION", e.printStackTrace().toString())
                    Thread.sleep(10000);
                    continue;
                }
            }
        }
        return view
    }


    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            PersonFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}