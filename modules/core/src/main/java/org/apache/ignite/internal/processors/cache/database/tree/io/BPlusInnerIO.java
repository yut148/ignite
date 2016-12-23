/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.cache.database.tree.io;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.pagemem.PageUtils;
import org.apache.ignite.internal.processors.cache.database.tree.util.PageHandler;

/**
 * Abstract IO routines for B+Tree inner pages.
 */
public abstract class BPlusInnerIO<L> extends BPlusIO<L> {
    /** */
    private static final int SHIFT_LEFT = ITEMS_OFF;

    /** */
    private static final int SHIFT_LINK = SHIFT_LEFT + 8;

    /** */
    private final int SHIFT_RIGHT = SHIFT_LINK + itemSize;

    /**
     * @param type Page type.
     * @param ver Page format version.
     * @param canGetRow If we can get full row from this page.
     * @param itemSize Single item size on page.
     */
    protected BPlusInnerIO(int type, int ver, boolean canGetRow, int itemSize) {
        super(type, ver, false, canGetRow, itemSize);
    }

    /** {@inheritDoc} */
    @Override public int getMaxCount(int pageSize, long buf) {
        // The structure of the page is the following:
        // |ITEMS_OFF|w|A|x|B|y|C|z|
        // where capital letters are data items, lowercase letters are 8 byte page references.
        return (pageSize - ITEMS_OFF - 8) / (itemSize + 8);
    }

    /**
     * @param buf Buffer.
     * @param idx Index.
     * @return Page ID.
     */
    public final long getLeft(long buf, int idx) {
        return PageUtils.getLong(buf, (offset(idx, SHIFT_LEFT)));
    }

    /**
     * @param buf Buffer.
     * @param idx Index.
     * @param pageId Page ID.
     */
    public final void setLeft(long buf, int idx, long pageId) {
        PageUtils.putLong(buf, offset(idx, SHIFT_LEFT), pageId);

        assert pageId == getLeft(buf, idx);
    }

    /**
     * @param buf Buffer.
     * @param idx Index.
     * @return Page ID.
     */
    public final long getRight(long buf, int idx) {
        return PageUtils.getLong(buf, offset(idx, SHIFT_RIGHT));
    }

    /**
     * @param buf Buffer.
     * @param idx Index.
     * @param pageId Page ID.
     */
    public final void setRight(long buf, int idx, long pageId) {
        PageUtils.putLong(buf, offset(idx, SHIFT_RIGHT), pageId);

        assert pageId == getRight(buf, idx);
    }

    /** {@inheritDoc} */
    @Override public final void copyItems(long src, long dst, int srcIdx, int dstIdx, int cnt,
        boolean cpLeft) throws IgniteCheckedException {
        assert srcIdx != dstIdx || src != dst;

        cnt *= itemSize + 8; // From items to bytes.

        if (dstIdx > srcIdx) {
            PageHandler.copyMemory(src, dst, offset(srcIdx), offset(dstIdx), cnt);

            if (cpLeft)
                PageUtils.putLong(dst, offset(dstIdx, SHIFT_LEFT), PageUtils.getLong(src, (offset(srcIdx, SHIFT_LEFT))));
        }
        else {
            if (cpLeft)
                PageUtils.putLong(dst, offset(dstIdx, SHIFT_LEFT), PageUtils.getLong(src, (offset(srcIdx, SHIFT_LEFT))));

            PageHandler.copyMemory(src, dst, offset(srcIdx), offset(dstIdx), cnt);
        }
    }

    /**
     * @param idx Index of element.
     * @param shift It can be either link itself or left or right page ID.
     * @return Offset from byte buffer begin in bytes.
     */
    private int offset(int idx, int shift) {
        return shift + (8 + itemSize) * idx;
    }

    /** {@inheritDoc} */
    @Override protected final int offset(int idx) {
        return offset(idx, SHIFT_LINK);
    }

    // Methods for B+Tree logic.

    /** {@inheritDoc} */
    @Override public void insert(
        long buf,
        int idx,
        L row,
        byte[] rowBytes,
        long rightId
    ) throws IgniteCheckedException {
        super.insert(buf, idx, row, rowBytes, rightId);

        // Setup reference to the right page on split.
        setRight(buf, idx, rightId);
    }

    /**
     * @param newRootBuf New root buffer.
     * @param newRootId New root ID.
     * @param leftChildId Left child ID.
     * @param row Moved up row.
     * @param rightChildId Right child ID.
     * @throws IgniteCheckedException If failed.
     */
    public void initNewRoot(
        long newRootBuf,
        long newRootId,
        long leftChildId,
        L row,
        byte[] rowBytes,
        long rightChildId,
        int pageSize
    ) throws IgniteCheckedException {
        initNewPage(newRootBuf, newRootId, pageSize);

        setCount(newRootBuf, 1);
        setLeft(newRootBuf, 0, leftChildId);
        store(newRootBuf, 0, row, rowBytes);
        setRight(newRootBuf, 0, rightChildId);
    }
}
