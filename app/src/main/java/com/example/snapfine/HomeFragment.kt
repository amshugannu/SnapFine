package com.example.snapfine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var headerView: View
private lateinit var userNameTextView: TextView


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment layout (which should have all views except plus_img and bottom_navigation)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize CardViews
        val cameraCardView = view.findViewById<CardView>(R.id.card_camera)
        val galleryCardView = view.findViewById<CardView>(R.id.card_gallery)
        val myComplaintsCardView = view.findViewById<CardView>(R.id.mycomplaints)
        val myViolationsCardView = view.findViewById<CardView>(R.id.myviolations)

        // Drawer toggle button (ImageButton)

        val buttonDrawerToggle = view.findViewById<ImageButton>(R.id.buttondrawertoggel)

        buttonDrawerToggle.setOnClickListener {
            // Access activity's DrawerLayout
            val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.drawerlayout)
            val drawerContainer = activity?.findViewById<View>(R.id.custom_drawer)

            if (drawerLayout != null && drawerContainer != null) {
                drawerLayout.openDrawer(drawerContainer)
            }
        }

        // CardView click listeners for navigation
        cameraCardView.setOnClickListener {
            val intent = Intent(requireContext(), CameraActivity::class.java)
            startActivity(intent)
        }

        galleryCardView.setOnClickListener {
            val intent = Intent(requireContext(), GalleryActivity::class.java)
            startActivity(intent)
        }

        myComplaintsCardView.setOnClickListener {
            val intent = Intent(requireContext(), MyComplaintsActivity::class.java)
            startActivity(intent)
        }

        myViolationsCardView.setOnClickListener {
            val fragment = HistoryFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Update drawer user name on view creation
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}