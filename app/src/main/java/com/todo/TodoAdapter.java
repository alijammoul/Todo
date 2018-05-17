package com.todo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;


public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<TodoItem> todoItemList;

    TodoAdapter(List<TodoItem> todoItems ) {
        this.todoItemList = todoItems;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View v = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_todo_complete, parent, false);
                return new TodoViewHolderComplete(v);

            case 2:
                View v2 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_todo_incomplete, parent, false);
                return new TodoViewHolderInComplete(v2);

            default:
                View v3 = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_todo_complete, parent, false);
                return new TodoViewHolderComplete(v3);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ((TodoViewHolderComplete) holder).bind(todoItemList.get(position));
                break;
            case 2:
                ((TodoViewHolderInComplete) holder).bind(todoItemList.get(position), position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        TodoItem i = todoItemList.get(position);
        if (i.isCompleted()) return 0;

        return 2;
    }

    @Override
    public long getItemId(int position) {
        return todoItemList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return todoItemList.size();
    }

    private class TodoViewHolderComplete extends RecyclerView.ViewHolder {
        TextView textView_title;
        public TodoViewHolderComplete(View itemView) {
            super(itemView);
            textView_title = (TextView) itemView.findViewById(R.id.textView_title);
        }

        void bind(TodoItem item) {
            textView_title.setText(item.getTitle());
        }
    }


    private class TodoViewHolderInComplete extends RecyclerView.ViewHolder {

        TextView textView_title;
        CheckBox checkbox_mark;

        public TodoViewHolderInComplete(View itemView) {
            super(itemView);
            textView_title = (TextView) itemView.findViewById(R.id.textView_title);
            checkbox_mark = (CheckBox) itemView.findViewById(R.id.checkbox_mark);
        }

        void bind(final TodoItem item, final int position){
            textView_title.setText(item.getTitle());
            checkbox_mark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AsyncTask<Context, Void, Void >() {
                        @Override
                        protected Void doInBackground(Context... params) {
                            Context c = params[0];
                            item.setCompleted(true);
                            AppDatabase.getAppDatabase(c).todoDao().markAsCompleted(item);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            notifyItemChanged(position);
                        }
                    }.execute(itemView.getContext());
                }
            });
        }
    }

}
