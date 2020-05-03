/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DNLO;

/**
 * The base class for a sortable linked list
 */

public class SortableLinkedListItem {
    public SortableLinkedListItem next = null;
    public double key;
    //--------------------------------------------------------------------------

    public SortableLinkedListItem() {
    }
    
    public SortableLinkedListItem(double key) {
        this.key = key;
    }
    
    private SortableLinkedListItem(SortableLinkedListItem child) {
        this.next = child;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * prints all keys starting from the current item as the list head
     */
    public void printKeys(String comment) {
        System.out.print(comment);
        SortableLinkedListItem current = this;
        while (current != null) {
            System.out.printf("%.3f, ", current.key);
            current = current.next;
        }
        System.out.println("END");
    }
    //--------------------------------------------------------------------------
    
    /**
     * Appends the item this to the list item pointed to by @param last (can be null)
     * and @returns the new 'last' item of the list.
     * Note that this.next is NOT modified !!!
     */
    public SortableLinkedListItem appendTo(SortableLinkedListItem last) {
        if (last != null) {
            last.next = this;
        }
        return this;
    }    
    //--------------------------------------------------------------------------
    
    /**
     * Transforms
     *       parentOfA -> a == itm -> itm -> itemX -> ...
     *       parentOfB -> b == itm -> itm -> itemY -> ...
     * into
     *       parentOfA -> itm -> itm -> itm -> itm == result -> itemX
     *       parentOfB ---------------------------------------> itemY
     * That is, parentOfA.next gets the first item of the merged list,
     * result gets the last item of the processed (and sorted) '(sub)chunk a' 
     * items, and parentOfB.next gets the item immediately following the 
     * processed items of the 'chunk b'
     * @param maxLen limits the maximum number of items to be taken from any
     * of chunks.
     */
    private static SortableLinkedListItem merge_core(
            SortableLinkedListItem parentOfA, SortableLinkedListItem parentOfB, 
            int maxLen, boolean ascendingly) 
    {        
        if ((parentOfA == null) || (parentOfB == null)) 
            return null; // In general, we're going to modify parentOfA.next and
        // parentOfB.next, so neither of parentOfA, parentOfB can be null
        
        SortableLinkedListItem a = parentOfA.next;
        SortableLinkedListItem b = parentOfB.next;
        
        if ((a == null) && (b == null)) 
            return null; // if both lists are empty -- nothing to be done!
                
        SortableLinkedListItem newHead = null, current = null;
        int usedFromA = 0, usedFromB = 0;
        SortableLinkedListItem tmp;
        
        // repeat until BOTH lists reach their ends OR element transfer number limit
        while (((a != null) && (usedFromA < maxLen)) ||
                ((b != null) && (usedFromB < maxLen))) {
            
            // Decide, from which list the element is to be taken
            boolean takeFromA = (b == null) || (usedFromB >= maxLen); // take elements
            // from a if the end or limit of the list b has been reached
            // Note that if takeFromA == true, the while() condition guarantees
            // that the list a end/limit has NOT been reached yet
            if (! takeFromA) {
                // in this case the list b still has elements. How about the list a?
                // If it still has elements, we have to compare a.key and b.key
                // to decide from which of the lists the element is to be taken
                // Otherwise, we can (and should) only take element from b => do not
                // change the current value of takeFromA == false
                if (((a != null) && (usedFromA < maxLen))) {
                    if (ascendingly)
                        takeFromA =  (a.key < b.key);
                    else
                        takeFromA =  (a.key > b.key);
                }
            }
                    
            if (takeFromA) {
                // take a and append it to the list
                tmp = a.next; // save the item pointed to by a
                current = a.appendTo(current);
                usedFromA++;
                if (newHead == null)
                    newHead = current; // if we've just created the very first item of the list
                a.next = null; // note that by this we also force current.next to become null
                a = tmp; // now a contains pointer to the next item in the 'chunk a'
            } else {
                // take b and append it to the list
                tmp = b.next; // save the item pointed to by b
                current = b.appendTo(current);
                usedFromB++;
                if (newHead == null)
                    newHead = current; // if we've just created the very first item of the list
                b.next = null; // note that by this we also force current.next to become null
                b = tmp; // now a contains pointer to the next item in the 'chunk b'
            }
        }
        // connect lists
        parentOfA.next = newHead;        
        current.next = a; // note that current is not null since the list 
        // starting with newHead is guaranteed to have at least 1 element
        parentOfB.next = b;
        
        return current;
    }
    /**
     * Sorts ascendingly the list pointed by @param first and
     * @returns a head item of the sorted list
     */
    public static SortableLinkedListItem sort(SortableLinkedListItem first, 
            boolean ascendingly) 
    {
        if (first == null)
            return null; // sorted empty list is an empty list
        
        if (first.next == null)
            return first; // sorted list containing only a single element is just this element
        
        // Note that now it is guaranteed that the list starting with first
        // contains at least 2 elements!
        
        SortableLinkedListItem tmpA = null; // will hold the _current_
        SortableLinkedListItem tmpB = null;        // element of each list

        // 'imaginary' (pre-)heads of the lists a and b
        SortableLinkedListItem parentOfA = new SortableLinkedListItem(null);
        SortableLinkedListItem parentOfB = new SortableLinkedListItem(null);
        
        // 1) Init: divide a list into wo lists                
        while (first != null) {
            tmpA = first.appendTo(tmpA);
            if (parentOfA.next == null)
                parentOfA.next = tmpA; // if we've just created the list
            first = first.next;
            
            if (first == null)
                break;
            
            tmpB = first.appendTo(tmpB);
            if (parentOfB.next == null)
                parentOfB.next = tmpB; // if we've just created the list
            first = first.next;            
        }
        
        // terminate both lists; not that since first->... list contained
        // at least two elements, neither of a and b can be null
        tmpA.next = null;
        tmpB.next = null;
        
        // below we re-use variables tmpA and tmpB as a 'sliding' heads        
        
        // 2) Do merges.
        SortableLinkedListItem lastInSortedChunk;
        int maxLen = 1;
        
        do {
            tmpA = parentOfA;
            lastInSortedChunk = parentOfB;
            
            //do {
            while (lastInSortedChunk != null) { // loop while the 'chunk b'
                // exists (even with no items)
                tmpB = lastInSortedChunk;
                lastInSortedChunk = merge_core(tmpA, tmpB, maxLen, ascendingly);
                //
                // tmpA -> (sorted1...sortedN=)=lastInSortedChunk -> lastInSortedChunk.next -> ...
                // tmpB -------------------------------------------> tmpB.next -> ...
                //
                // Now we're going to do the same with 'chink B' and 'insert'
                // the sorted fragment composed of items from 
                // lastInSortedChunk.next->...  and  tmpB.next->... into 
                // tmpB.next; hence it makes no difference, whether tmpB.next 
                // is null or not. Note however that if lastInSortedChunk.next
                // contains some items, they still _MUST_ be transfered
                // into tmpB.next in order to 'equalize' the lists lengths.
                // But if it appears that lastInSortedChunk == null [which can
                // happen only if tmpA==0 or tmpB==0 OR if tmpA.next==0 and
                // tmpB.next==0, i.e., for empty lists], that means that there
                // exist no items suitable for insertion into 'chunk b' =>
                // there is nothing to be done => no need to call merge_core()
                if (lastInSortedChunk != null) {
                    tmpA = lastInSortedChunk;
                    lastInSortedChunk = merge_core(tmpB, tmpA, maxLen, ascendingly);
                    //
                    // tmpA -------------------------------------------> tmpA.next -> ...                
                    // tmpB -> (sorted1...sortedN=)=lastInSortedChunk -> lastInSortedChunk.next -> ...
                    //
                    // Again, it does not matter, whether tmpA.next == null or not.
                    // At the next step we're going to insert some of the items 
                    // pointed to by lastInSortedChunk.next into 'chunk a';
                    // there is no problem if lastInSortedChunk.next == null
                    // provided that lastInSortedChunk != null.
                    // But if lastInSortedChunk == null, we have no more items
                    // to process => we've finished!                    
                }                                
            }
            
            maxLen *= 2;
        } while (parentOfB.next != null);// maxLen < totElements);

        return parentOfA.next;
    }
}
