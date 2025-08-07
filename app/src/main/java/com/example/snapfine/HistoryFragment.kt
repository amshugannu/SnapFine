package com.example.snapfine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ViolationAdapter
    private lateinit var backbtn: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate fragment layout
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewMyCases)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Assume you pass an argument called "showBackBtn" when navigating by card
        backbtn = view.findViewById(R.id.btn_back)
        backbtn?.setOnClickListener {
            val isInBackStack = parentFragmentManager.backStackEntryCount > 0
            if (isInBackStack) {
                parentFragmentManager.popBackStack()
            } else {
                (activity as? HomeActivity)?.let { homeActivity ->
                    if (!homeActivity.isHomeFragmentActive()) {
                        homeActivity.switchToHomeFragment()
                    } else {
                        activity?.onBackPressed()
                    }
                }
            }
        }


        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Example query: Fetch all violations reported by the current user (as historical data)
        val query: Query = FirebaseFirestore.getInstance()
            .collection("violations")
            .whereEqualTo("reportedToUID", currentUserUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<Violation>()
            .setQuery(query, Violation::class.java)
            .build()

        adapter = ViolationAdapter(options)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        if (::adapter.isInitialized) {
            adapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::adapter.isInitialized) {
            adapter.stopListening()
        }
    }
}



