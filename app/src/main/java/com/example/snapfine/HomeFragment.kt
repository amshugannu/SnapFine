package com.example.snapfine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import android.widget.Toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        val btnReportNow = view.findViewById<View>(R.id.btn_report_now)
        val profileIcon = view.findViewById<View>(R.id.ivProfile)
        val complaintsCard = view.findViewById<View>(R.id.card_my_complaints)
        val violationsCard = view.findViewById<View>(R.id.card_my_violations)

        // Setup Action Cards UI
        complaintsCard.findViewById<android.widget.TextView>(R.id.tvActionTitle).text = "My Complaints"
        complaintsCard.findViewById<android.widget.TextView>(R.id.tvActionSubtitle).text = "Track your reports"
        complaintsCard.findViewById<android.widget.ImageView>(R.id.ivActionIcon).setImageResource(R.drawable.baseline_edit_24)

        violationsCard.findViewById<android.widget.TextView>(R.id.tvActionTitle).text = "My Violations"
        violationsCard.findViewById<android.widget.TextView>(R.id.tvActionSubtitle).text = "View issued fines"
        violationsCard.findViewById<android.widget.ImageView>(R.id.ivActionIcon).setImageResource(R.drawable.violations_img)

        // Setup Alerts Header
        view.findViewById<android.widget.TextView>(R.id.tvSectionTitle).text = "Traffic Alerts 🚨"

        // Click listeners
        btnReportNow.applyScaleAnimation()
        btnReportNow.setOnClickListener {
            handleReportClick()
        }

        profileIcon.setOnClickListener {
            val intent = Intent(requireContext(), MyProfileActivity::class.java)
            startActivity(intent)
        }

        if (UserSession.role != "citizen") {
            btnReportNow.visibility = View.GONE
        }

        complaintsCard.applyScaleAnimation()
        complaintsCard.setOnClickListener {
            val intent = Intent(requireContext(), MyComplaintsActivity::class.java)
            startActivity(intent)
        }

        violationsCard.applyScaleAnimation()
        violationsCard.setOnClickListener {
            val fragment = HistoryFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun handleReportClick(isGallery: Boolean = false) {
        if (UserSession.isPending()) {
            Toast.makeText(requireContext(), "Your profile is under review", Toast.LENGTH_SHORT).show()
        } else if (UserSession.isRejected()) {
            Toast.makeText(requireContext(), "Your profile was rejected. Contact support", Toast.LENGTH_SHORT).show()
        } else {
            val intent = if (isGallery) {
                Intent(requireContext(), GalleryActivity::class.java)
            } else {
                Intent(requireContext(), CameraActivity::class.java)
            }
            startActivity(intent)
        }
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
