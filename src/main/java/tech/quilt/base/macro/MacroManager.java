package tech.quilt.base.macro;

import com.google.common.reflect.TypeToken;
import java.util.HashSet;
import java.util.Set;
import tech.quilt.base.filemanager.api.ManagerFileAbstract;

public class MacroManager extends ManagerFileAbstract<Macro> {
   public MacroManager() {
      super("macro.json", "", (new TypeToken<Set<Macro>>() {
      }).getType(), HashSet::new);
   }

   public boolean removeMacro(Macro macro) {
      return this.getItems().remove(macro);
   }
}
