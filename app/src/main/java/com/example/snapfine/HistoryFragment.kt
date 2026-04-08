package com.example.snapfine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
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

        adapter = ViolationAdapter()
        recyclerView.adapter = adapter

        // Dashboard Diagnosis: Temporarily remove orderBy to bypass index requirement
        android.util.Log.d("SnapFine-DEBUG", "[DEBUG-OFFENDER] --- OFFENDER QUERY DIAGNOSIS ---")
        android.util.Log.d("SnapFine-DEBUG", "[DEBUG-OFFENDER] Current User UID: $currentUserUid")

        val db = FirebaseFirestore.getInstance()
        registration = db.collection("violations")
            .whereEqualTo("violatorUid", currentUserUid)
            .whereEqualTo("status", "approved")
            // .orderBy("timestamp", Query.Direction.DESCENDING) // TEMPORARILY REMOVED FOR INDEX TESTING
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    android.util.Log.e("SnapFine-DEBUG", "[DEBUG-OFFENDER] Listen failed. Error: ${e.message}", e)
                    // SHOW ERROR ON UI so user can see 'Index Required' or other errors
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Firebase Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    android.util.Log.d("SnapFine-DEBUG", "[DEBUG-OFFENDER] Snapshot received. Document count: ${snapshots.size()}")
                    
                    val rawDocsInfo = snapshots.documents.map { doc ->
                        "ID: ${doc.id}, violatorUid: ${doc.getString("violatorUid")}, status: ${doc.getString("status")}"
                    }.joinToString("\n")
                    android.util.Log.d("SnapFine-DEBUG", "[DEBUG-OFFENDER] Raw Documents:\n$rawDocsInfo")

                    val violationsList = snapshots.toObjects(Violation::class.java)
                    android.util.Log.d("SnapFine-DEBUG", "[DEBUG-OFFENDER] Step 6 & 9 Check: Fetched ${violationsList.size} objects for violator $currentUserUid")
                    
                    // Step 7: Force UI Refresh
                    adapter.updateData(violationsList)
                } else {
                    android.util.Log.d("SnapFine-DEBUG", "[DEBUG-OFFENDER] Snapshot is NULL")
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
