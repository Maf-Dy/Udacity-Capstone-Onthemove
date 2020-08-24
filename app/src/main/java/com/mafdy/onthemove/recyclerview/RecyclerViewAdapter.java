package com.mafdy.onthemove.recyclerview;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


import androidx.appcompat.widget.SwitchCompat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.widget.CompoundButton;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDoNothing;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.mafdy.onthemove.MainActivity;
import com.mafdy.onthemove.R;
import com.mafdy.onthemove.database.AppDatabase;
import com.mafdy.onthemove.database.Status;

import static com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND;
import static com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants.REACTION_CAN_SWIPE_LEFT;


/**
 * A custom adapter to use with the RecyclerView widget.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SwipeableItemAdapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Status> modelList;

    private OnItemClickListener mItemClickListener;

    private OnCheckedListener mOnCheckedListener;


    private Set<Integer> checkSet = new HashSet<>();


    public RecyclerViewAdapter(Context context, List<Status> modelList) {
        this.mContext = context;
        this.modelList = modelList;
        setHasStableIds(true);
    }

    public void updateList(List<Status> modelList) {
        this.modelList = modelList;
        notifyDataSetChanged();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recycler_list, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        //Here you can fill your row view
        if (holder instanceof ViewHolder) {
            final Status model = getItem(position);
            ViewHolder genericViewHolder = (ViewHolder) holder;



            genericViewHolder.itemTxtTitle.setText(model.getTransition() + " " + model.getActivity() + " " + mContext.getText(R.string.at_java_adapter)  + " " + model.getLocationaddress());
            genericViewHolder.itemTxtMessage.setText(new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy").format(model.getDatetime().getTime()) + " " + mContext.getText(R.string.accurate_to_java_adapter) + new DecimalFormat("##.###").format(model.getLocationaccuracy()) );


            if(model.getActivity().equals("On Foot") ||
                    model.getActivity().equals("Walking") ||
                    model.getActivity().equals("Still") ||
            model.getActivity().equals("Running")
                    )
            {
                genericViewHolder.imgUser.setImageResource(R.drawable.ic_directions_walk_black_24dp);
            }
            else if(model.getActivity().equals("In Vehicle"))
            {
                genericViewHolder.imgUser.setImageResource(R.drawable.ic_directions_car_black_24dp);
            }




        }
    }


    @Override
    public int getItemCount() {

        return modelList.size();
    }

    @Override
    public long getItemId(int position) {

        return modelList.get(position).getId();
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void SetOnCheckedListener(final OnCheckedListener onCheckedListener) {
        this.mOnCheckedListener = onCheckedListener;

    }

    private Status getItem(int position) {
        return modelList.get(position);
    }

    @Override
    public int onGetSwipeReactionType(RecyclerView.ViewHolder holder, int position, int x, int y) {
        return REACTION_CAN_SWIPE_LEFT;
    }

    @Override
    public void onSwipeItemStarted(RecyclerView.ViewHolder holder, int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onSetSwipeBackground(RecyclerView.ViewHolder holder, int position, int type) {

        if (type == DRAWABLE_SWIPE_LEFT_BACKGROUND) {
            holder.itemView.setBackgroundColor(Color.YELLOW);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public SwipeResultAction onSwipeItem(RecyclerView.ViewHolder holder, final int position, int result) {
        if (result == SwipeableItemConstants.RESULT_SWIPED_LEFT) {
            return new SwipeResultActionMoveToSwipedDirection() {
                @Override
                protected void onPerformAction() {
                    super.onPerformAction();

                    MainActivity mainActivity = (MainActivity) mContext;


                    mainActivity.mStatusViewModel.delete(modelList.get(position));
                   // AppDatabase.getInstance(mContext).status().deleteStatus(modelList.get(position));
                    RecyclerViewAdapter.this.notifyDataSetChanged();

                }
            };
        } else {
            return new SwipeResultActionDoNothing();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Status model);
    }


    public interface OnCheckedListener {
        void onChecked(View view, boolean isChecked, int position, Status model);
    }

    public class ViewHolder extends AbstractSwipeableItemViewHolder {

        private ImageView imgUser;
        private TextView itemTxtTitle;
        private TextView itemTxtMessage;


        FrameLayout containerView;


        public ViewHolder(final View itemView) {
            super(itemView);

            // ButterKnife.bind(this, itemView);

            this.imgUser = (ImageView) itemView.findViewById(R.id.img_activity);
            this.itemTxtTitle = (TextView) itemView.findViewById(R.id.item_txt_title);
            this.itemTxtMessage = (TextView) itemView.findViewById(R.id.item_txt_info);


            containerView = (FrameLayout) itemView.findViewById(R.id.container);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemClickListener.onItemClick(itemView, getAdapterPosition(), modelList.get(getAdapterPosition()));


                }
            });

        }

        @Override
        public View getSwipeableContainerView() {
            return containerView;
        }
    }


}



