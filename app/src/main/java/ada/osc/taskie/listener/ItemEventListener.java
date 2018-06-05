package ada.osc.taskie.listener;

import ada.osc.taskie.model.Task;

public interface ItemEventListener {
   void onTaskSwipeRight(Task task);
   void onTaskSwipeLeft(Task task);
   void onToggleClick(Task task);
   void onFavouriteTaskStarClick(Task task);
}
