/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package jcuda.driver;

import jcuda.NativePointerObject;
import jcuda.runtime.cudaArray;

/**
 * Java port of a CUarray
 *
 * @see jcuda.driver.JCudaDriver#cuArrayCreate
 * @see jcuda.driver.JCudaDriver#cuArrayGetDescriptor
 * @see jcuda.driver.JCudaDriver#cuArray3DCreate
 * @see jcuda.driver.JCudaDriver#cuArray3DGetDescriptor
 * @see jcuda.driver.JCudaDriver#cuArrayDestroy
 */
public class CUarray extends NativePointerObject
{
    /**
     * Creates a new, uninitialized CUarray
     */
    public CUarray()
    {
    }

    /**
     * Creates a CUarray for the given {@link cudaArray}. This
     * corresponds to casting a cudaArray to a CUarray.
     * 
     * @param array The other array
     */
    public CUarray(cudaArray array)
    {
        super(array);
    }
    
    /**
     * Returns a String representation of this object.
     *
     * @return A String representation of this object.
     */
    @Override
    public String toString()
    {
        return "CUarray["+
            "nativePointer=0x"+Long.toHexString(getNativePointer())+"]";
    }

}
