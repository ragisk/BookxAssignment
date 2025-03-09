package com.example.bookxpertassignment

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookxpertassignment.model.Account

abstract class AccountAdapter(private var accountList: ArrayList<Account>, var context: Context) :
    RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountName: TextView = itemView.findViewById(R.id.accountNameTV)
        val accountId: TextView = itemView.findViewById(R.id.accountIdTV)
        val deleteIV: ImageView = itemView.findViewById(R.id.deleteIV)
        val updateIV: ImageView = itemView.findViewById(R.id.updateIV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accountList[position]
        holder.accountName.text = account.alternateName ?: account.ActName
        holder.accountId.text = account.actid.toString()
        holder.deleteIV.setOnClickListener {
            showDeleteDialog(context, position)
        }

        holder.updateIV.setOnClickListener {
            showUpdateDialog(context, position)
        }
    }

    override fun getItemCount(): Int {
        return accountList.size
    }


    abstract fun delete(account: Account)
    abstract fun update(account: Account)

    private fun showDeleteDialog(context: Context, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.delete_account))
        builder.setMessage(context.getString(R.string.delete_warning))

        builder.setPositiveButton(context.getString(R.string.id)) { dialog, _ ->
            // Remove item from the list
            accountList.removeAt(position)
            delete(accountList[position])
            // Notify adapter
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, accountList.size)

            dialog.dismiss()
        }

        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }

    fun showUpdateDialog(context: Context, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_account, null)

        val accountNameEditText = dialogView.findViewById<EditText>(R.id.accountNameEditText)
        val alternateNameEditText = dialogView.findViewById<EditText>(R.id.alternateNameEditText)
        val account = accountList[position]
        accountNameEditText.setText(account.ActName)
        alternateNameEditText.setText(account.alternateName ?: "")

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.update_account))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.update)) { dialog, _ ->
                val updatedAccount = account.copy(
                    ActName = accountNameEditText.text.toString(),
                    alternateName = alternateNameEditText.text.toString()
                )
                accountList[position] = updatedAccount
                update(updatedAccount)
                notifyDataSetChanged()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}
