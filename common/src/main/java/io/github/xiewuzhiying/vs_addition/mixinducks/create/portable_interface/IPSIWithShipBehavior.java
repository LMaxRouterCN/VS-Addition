package io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.lang.Lang;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;

public interface IPSIWithShipBehavior {

    void setController(PortableStorageInterfaceWithShipController controller);

    PortableStorageInterfaceWithShipController getController();

    ScrollOptionBehaviour<WorkigMode> getWorkingMode();

    enum WorkigMode implements INamedIconOptions {

        ORIGINAL(AllIcons.I_MOVE_PLACE_RETURNED),
        WITH_SHIP(AllIcons.I_MOVE_PLACE),

        ;

        private String translationKey;
        private AllIcons icon;

        private WorkigMode(AllIcons icon) {
            this.icon = icon;
            translationKey = "vs_addition.working_mode." + Lang.asId(name());
        }

        public com.simibubi.create.foundation.gui.AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

    }
}