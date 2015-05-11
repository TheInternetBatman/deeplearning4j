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

/**
 * Java port of a CUdevice.
 *
 * @see jcuda.driver.JCudaDriver#cuDeviceGet
 * @see jcuda.driver.JCudaDriver#cuDeviceComputeCapability
 * @see jcuda.driver.JCudaDriver#cuDeviceGetAttribute
 * @see jcuda.driver.JCudaDriver#cuDeviceGetCount
 * @see jcuda.driver.JCudaDriver#cuDeviceGetName
 * @see jcuda.driver.JCudaDriver#cuDeviceGetProperties
 * @see jcuda.driver.JCudaDriver#cuDeviceTotalMem
 */
public class CUdevice extends NativePointerObject
{
    /**
     * Creates a new, uninitialized CUdevice
     */
    public CUdevice()
    {
    }

    /**
     * Returns a String representation of this object.
     *
     * @return A String representation of this object.
     */
    @Override
    public String toString()
    {
        return "CUdevice["+
            "nativePointer=0x"+Long.toHexString(getNativePointer())+"]";
    }

}
