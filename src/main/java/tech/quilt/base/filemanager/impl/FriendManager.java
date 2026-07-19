package tech.quilt.base.filemanager.impl;

import com.google.common.reflect.TypeToken;
import java.util.HashSet;
import java.util.Set;
import tech.quilt.base.filemanager.api.ManagerFileAbstract;

public class FriendManager extends ManagerFileAbstract<String> {
   public FriendManager() {
      super("friends.json", "", (new TypeToken<Set<String>>() {
      }).getType(), HashSet::new);
   }

   public boolean isFriend(String name) {
      return this.getItems().contains(name);
   }

   public boolean removeFriend(String name) {
      return this.getItems().remove(name);
   }
}
