/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmcorej;

		import java.lang.Iterable;
		import java.util.Iterator;
		import java.util.NoSuchElementException;
		import java.lang.UnsupportedOperationException;
	
public class BooleanVector implements  Iterable<Boolean> {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected BooleanVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(BooleanVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        MMCoreJJNI.delete_BooleanVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

	
		public Iterator<Boolean> iterator() {
			return new Iterator<Boolean>() {
			
				private int i_=0;
			
				public boolean hasNext() {
					return (i_<size());
				}
				
				public Boolean next() throws NoSuchElementException {
					if (hasNext()) {
						++i_;
						return get(i_-1);
					} else {
					throw new NoSuchElementException();
					}
				}
					
				public void remove() throws UnsupportedOperationException {
					throw new UnsupportedOperationException();
				}		
			};
		}
		
		public Boolean[] toArray() {
			if (0==size())
				return new Boolean[0];
			
			Boolean strs[] = new Boolean[(int) size()];
			for (int i=0; i<size(); ++i) {
				strs[i] = get(i);
			}
			return strs;
		}
		
	
  public BooleanVector() {
    this(MMCoreJJNI.new_BooleanVector__SWIG_0(), true);
  }

  public BooleanVector(long n) {
    this(MMCoreJJNI.new_BooleanVector__SWIG_1(n), true);
  }

  public long size() {
    return MMCoreJJNI.BooleanVector_size(swigCPtr, this);
  }

  public long capacity() {
    return MMCoreJJNI.BooleanVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    MMCoreJJNI.BooleanVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return MMCoreJJNI.BooleanVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    MMCoreJJNI.BooleanVector_clear(swigCPtr, this);
  }

  public void add(boolean x) {
    MMCoreJJNI.BooleanVector_add(swigCPtr, this, x);
  }

  public boolean get(int i) {
    return MMCoreJJNI.BooleanVector_get(swigCPtr, this, i);
  }

  public void set(int i, boolean val) {
    MMCoreJJNI.BooleanVector_set(swigCPtr, this, i, val);
  }

}
