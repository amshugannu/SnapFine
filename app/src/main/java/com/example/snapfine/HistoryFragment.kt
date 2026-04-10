package com.example.snapfine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ViolationAdapter
    private lateinit var backbtn: ImageButton
    private lateinit var emptyStateView: View
    private lateinit var loadingStateView: View
    private var registration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewMyCases)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        recyclerView.itemAnimator = null

        loadingStateView = view.findViewById(R.id.loadingState)
        emptyStateView = view.findViewById(R.id.emptyState)

        // Configure Empty State
        view.findViewById<TextView>(R.id.tvEmptyStateTitle)?.text = getString(R.string.empty_violations_title)
        view.findViewById<TextView>(R.id.tvEmptyStateSubtitle)?.text = getString(R.string.empty_violations_subtitle)
        view.findViewById<ImageView>(R.id.ivEmptyStateIcon)?.setImageResource(R.drawable.baseline_edit_24)

        backbtn = view.findViewById(R.id.btn_back)
        backbtn?.setOnClickListener {
            val isInBackStack = parentFragmentManager.backStackEntryCount > 0
            if (isInBackStack) {
                parentFragmentManager.popBackStack()
            } else {
                activity?.onBackPressed()
            }
        }

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = ViolationAdapter()
        recyclerView.adapter = adapter

        // Show Initial Loading
        loadingStateView.visibility = View.VISIBLE
        emptyStateView.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val db = FirebaseFirestore.getInstance()
        registration = db.collection("violations")
            .whereEqualTo("violatorUid", currentUserUid)
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshots, e ->
                // Hide Loading once first response arrives
                loadingStateView.visibility = View.GONE

                if (e != null) {
                    android.util.Log.e("SnapFine-DEBUG", "Listen failed. Error: ${e.message}", e)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Firebase Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val violationsList = snapshots.toObjects(Violation::class.java)
                    adapter.updateData(violationsList)
                    
                    if (violationsList.isEmpty()) {
                        emptyStateView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyStateView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        registration?.remove()
    }

    override fun onDestroyView() {
        if (::recyclerView.isInitialized) {
            recyclerView.adapter = null
        }
        super.onDestroyView()
    }
}
