package com.example.libruary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class UserInfoFragment : Fragment() {

    private lateinit var logoutButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_info, container, false)

        // Initialize the logout button
        logoutButton = view.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("LibruaryPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as UserProfileActivity).userViewModel.username.observe(viewLifecycleOwner) { username ->
            view.findViewById<TextView>(R.id.usernameTextView).text = username
        }
    }

    fun updateUserInfo(username: String) {
        view?.findViewById<TextView>(R.id.usernameTextView)?.text = username
    }
}
