package com.minimalist.todo.fragment

import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.minimalist.todo.activity.MainActivity
import com.minimalist.todo.R
import com.minimalist.todo.adapter.GroceryAdapter
import com.minimalist.todo.db.TodoEntity
import com.minimalist.todo.model.Todo
import com.minimalist.todo.utils.snackBar
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_add_grocery.view.*
import kotlinx.android.synthetic.main.fragment_todo.*

class GroceryFragment: TodoFragment() {

    private lateinit var adapter: GroceryAdapter
    private var groceryList: MutableList<Todo> = ArrayList()

    var compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        getGrocery()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun setupRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(activity)
        adapter = GroceryAdapter(groceryList, { onItemChecked(it) }, { onItemDeleted(it) })
        recycler_view.adapter = adapter
    }

    fun addGroceryDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_grocery, null)
        val dialog = AlertDialog.Builder(activity as MainActivity, R.style.DialogTheme).setView(view).create()
        view.button_add_grocery.setOnClickListener {
            if(view.edit_text_grocery.text.isNotBlank()) {
                insertTodo(TodoEntity(view.edit_text_grocery.text.toString(), false, "Grocery", null))
                dialog.dismiss()
            }
        }
        dialog.window.setGravity(Gravity.BOTTOM)
        dialog.show()
    }


    private fun getGrocery() {
        val disposable = getTodo("Grocery")
                .subscribe {
                    if (view != null) {
                        groceryList.clear()
                        it.forEach { groceryList.add(Todo(it.id, it.todo, it.done, it.type, it.dateTime)) }
                        emptyView()
                        adapter.notifyDataSetChanged()
                    }
                }
        compositeDisposable.add(disposable)
    }

    private fun onItemChecked(todo: Todo) {
        if(!recycler_view.isComputingLayout) {
            if(todo.done) updateTodo(todo, false) else updateTodo(todo, true)
        }
    }

    private fun onItemDeleted(selected: List<Int>) {
        deleteTodo(selected, groceryList)
        getGrocery()
        snackBar(coordinator_layout, "${selected.size} item deleted", "UNDO") {
            undoDeleteTodo()
            getGrocery()
        }
    }

    override fun destroyActionCallback() {
        adapter.deleteActionMode.actionMode?.finish()
        adapter.deleteActionMode.actionMode = null
    }

    private fun emptyView() {
        empty_todo.setImageResource(R.drawable.empty_warehouse)
        if(groceryList.isEmpty()) {
            empty_todo.visibility = View.VISIBLE
        } else {
            empty_todo.visibility = View.GONE
        }
    }

}
