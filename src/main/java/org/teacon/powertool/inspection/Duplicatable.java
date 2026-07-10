package org.teacon.powertool.inspection;

import org.jspecify.annotations.NonNull;

/**
 * 用于表示某个类是可被复制粘贴的对象
 * */
public interface Duplicatable {

    @NonNull Duplicatable duplicate();

    default void paste(@NonNull Duplicatable copy) {

    }

}
