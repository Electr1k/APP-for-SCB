package com.example.appforsb.adapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appforsb.PatentActivity;
import com.example.appforsb.R;
import com.example.appforsb.model.Patent;
import java.util.List;

public class PatentAdapter extends RecyclerView.Adapter<PatentAdapter.PatentViewHolder>{
    // Адаптер для прогрузки кафешек на главной
    Context context;
    List<Patent> patents;
    // конструктор
    public PatentAdapter(Context context, List<Patent> patents) {
        this.context = context;
        this.patents = patents;
    }

    @NonNull
    @Override
    public PatentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View patentItem = LayoutInflater.from(context).inflate(R.layout.activity_patent_item, parent,false);
        return new PatentAdapter.PatentViewHolder(patentItem);
    }

    // Установка View'сов для карточки + onClickListener
    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull PatentViewHolder holder, @SuppressLint("RecyclerView") int position) {
            String invertorCard = patents.get(position).getInventorCard();
            String number = patents.get(position).getNumber();
            String name = patents.get(position).getName();
            String publication_date = patents.get(position).getPublication_date();
            String filing_date = patents.get(position).getFiling_date();
            String ipc = patents.get(position).getIpc();
            String inventor = patents.get(position).getInventor();
            String patente = patents.get(position).getPatente();
            String priority = patents.get(position).getPriority();
            String description = patents.get(position).getDescription();
            String id = patents.get(position).getId();
            holder.name.setText(Html.fromHtml(name));
            holder.author.setText(invertorCard);
            holder.number.setText(number);
            holder.ipc.setText(ipc);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PatentActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("inventor", inventor);
                    intent.putExtra("number", number);
                    intent.putExtra("name", name);
                    intent.putExtra("publication_date", publication_date);
                    intent.putExtra("filing_date", filing_date);
                    intent.putExtra("patente", patente);
                    intent.putExtra("ipc", ipc);
                    intent.putExtra("priority", priority);
                    intent.putExtra("description", description);
                    context.startActivity(intent);
                }
            });
    }
    // кол-во карточек
    @Override
    public int getItemCount() {
        return patents.size();
    }
    // Инициализация View'ов
    public static final class PatentViewHolder extends RecyclerView.ViewHolder{
        TextView name, author, number, ipc;
        public PatentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            author = itemView.findViewById(R.id.author);
            number = itemView.findViewById(R.id.number);
            ipc = itemView.findViewById(R.id.ipc);
        }
    }
}
