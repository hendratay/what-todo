package com.example.user.whattodo.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.user.whattodo.App
import com.example.user.whattodo.R
import com.example.user.whattodo.db.TodoDatabase
import com.example.user.whattodo.model.Todo
import javax.inject.Inject

class TodoRemoteViewsFactory(val context: Context, val intent: Intent?): RemoteViewsService.RemoteViewsFactory {

    @Inject lateinit var database : TodoDatabase

    private var widgetList: MutableList<Todo> = ArrayList()

    override fun onCreate() {
        App.component.inject(this)
        updateWidgetListView()
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onDataSetChanged() {
        updateWidgetListView()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.todo_widget_list_item)

        val fillIntent = Intent()
        Intent().putExtra(TodoWidget.EXTRA_ITEM, position)
        remoteViews.setOnClickFillInIntent(R.id.appwidget_list_item, fillIntent)

        when(widgetList[position].type) {
            context.getString(R.string.task) -> remoteViews.setImageViewResource(R.id.appwidget_list_item_icon, R.drawable.ic_send_black_24dp)
            context.getString(R.string.reminder) -> remoteViews.setImageViewResource(R.id.appwidget_list_item_icon, R.drawable.ic_event_available_black_24dp)
            context.getString(R.string.grocery) -> remoteViews.setImageViewResource(R.id.appwidget_list_item_icon, R.drawable.ic_shopping_cart_black_24dp)
        }
        remoteViews.setTextViewText(R.id.appwidget_list_item_text, widgetList[position].todoText.capitalize())

        return remoteViews
    }

    override fun getCount(): Int {
        return widgetList.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {
        widgetList.clear()
    }

    private fun updateWidgetListView() {
        database.todoDao().getAllTodo()
                .subscribe {
                    widgetList.clear()
                    it.forEach { widgetList.add(Todo(it.id, it.todo, it.done, it.type, it.dateTime)) }
                }
    }

}