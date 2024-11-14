package com.alterpat.financemanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.alterpat.financemanagement.databinding.ActivityDetailedBinding
import kotlinx.coroutines.launch

class DetailedActivity : AppCompatActivity() {
    private lateinit var transaction: Transaction
    private lateinit var binding: ActivityDetailedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityDetailedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transaction = intent.getSerializableExtra("transaction") as Transaction

        // Set initial values in input fields
        binding.labelInput.setText(transaction.label)
        binding.amountInput.setText(transaction.amount.toString())
        binding.descriptionInput.setText(transaction.description)

        // Hide the keyboard when clicking outside of an input field
        binding.root.setOnClickListener { view ->
            this.window.decorView.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // Set up TextWatchers for input fields
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Show the update button if any field is changed
                binding.updateBtn.visibility = View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.labelInput.addTextChangedListener(textWatcher)
        binding.amountInput.addTextChangedListener(textWatcher)
        binding.descriptionInput.addTextChangedListener(textWatcher)

        // Set up button click listener to handle the update
        binding.updateBtn.setOnClickListener {
            val label = binding.labelInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val amount = binding.amountInput.text.toString().toDoubleOrNull()

            // Input validation
            if (label.isEmpty()) {
                binding.labelLayout.error = "Please enter a valid label"
            } else if (amount == null) {
                binding.amountLayout.error = "Please enter a valid amount"
            } else {
                binding.labelLayout.error = null
                binding.amountLayout.error = null

                // Only update fields if they are changed
                val updatedTransaction = Transaction(
                    id = transaction.id,
                    label = if (label != transaction.label) label else transaction.label,
                    amount = if (amount != transaction.amount) amount else transaction.amount,
                    description = if (description != transaction.description) description else transaction.description
                )

                // Log values before update
                Log.d("DetailedActivity", "Attempting to update transaction with values: $updatedTransaction")

                // Call update method and send result back
                updateAndReturn(updatedTransaction)
            }
        }

        binding.closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun updateAndReturn(transaction: Transaction) {
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()

        lifecycleScope.launch {
            db.transactionDao().update(transaction)

            // Log update success
            Log.d("DetailedActivity", "Transaction updated successfully in the database")

            // Return the updated transaction to the previous activity
            val resultIntent = Intent()
            resultIntent.putExtra("updatedTransaction", transaction)
            setResult(RESULT_OK, resultIntent)

            // Log result intent data
            Log.d("DetailedActivity", "Returning updated transaction data to the previous activity")
            finish()
        }
    }
}
