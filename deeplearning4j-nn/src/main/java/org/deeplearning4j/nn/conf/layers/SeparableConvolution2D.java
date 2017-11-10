package org.deeplearning4j.nn.conf.layers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.ParamInitializer;
import org.deeplearning4j.nn.api.layers.LayerConstraint;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.layers.convolution.SeparableConvolution2DLayer;
import org.deeplearning4j.nn.params.ConvolutionParamInitializer;
import org.deeplearning4j.nn.params.SeparableConvolutionParamInitializer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.util.ConvolutionUtils;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.*;

/**
 * 2D Separable convolution layer configuration.
 *
 * Separable convolutions split a regular convolution operation into two
 * simpler operations, which are usually computationally more efficient.
 *
 * The first step in a separable convolution is a depth-wise convolution, which
 * operates on each of the input maps separately. A depth multiplier is used to
 * specify the number of outputs per input map in this step. This convolution
 * is carried out with the specified kernel sizes, stride and padding values.
 *
 * The second step is a point-wise operation, in which the intermediary outputs
 * of the depth-wise convolution are mapped to the desired number of feature
 * maps, by using a 1x1 convolution.
 *
 * The result of chaining these two operations will result in a tensor of the
 * same shape as that for a standard conv2d operation.
 *
 * @author Max Pumperla
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SeparableConvolution2D extends ConvolutionLayer {

    int depthMultiplier;

    /**
     * SeparableConvolution2D layer
     * nIn in the input layer is the number of channels
     * nOut is the number of filters to be used in the net or in other words the depth
     * The builder specifies the filter/kernel size, the stride and padding
     * The pooling layer takes the kernel size
     */
    protected SeparableConvolution2D(Builder builder) {
        super(builder);
        this.hasBias = builder.hasBias;
        this.depthMultiplier = builder.depthMultiplier;
        this.convolutionMode = builder.convolutionMode;
        this.dilation = builder.dilation;
        if (builder.kernelSize.length != 2)
            throw new IllegalArgumentException("Kernel size of should be rows x columns (a 2d array)");
        this.kernelSize = builder.kernelSize;
        if (builder.stride.length != 2)
            throw new IllegalArgumentException("Stride should include stride for rows and columns (a 2d array)");
        this.stride = builder.stride;
        if (builder.padding.length != 2)
            throw new IllegalArgumentException("Padding should include padding for rows and columns (a 2d array)");
        this.padding = builder.padding;
        this.cudnnAlgoMode = builder.cudnnAlgoMode;
        this.cudnnFwdAlgo = builder.cudnnFwdAlgo;
        this.cudnnBwdFilterAlgo = builder.cudnnBwdFilterAlgo;
        this.cudnnBwdDataAlgo = builder.cudnnBwdDataAlgo;

        initializeConstraints(builder.allParamConstraints, builder.weightConstraints, builder.biasConstraints,
                builder.pointWiseConstraints);
    }

    protected void initializeConstraints(List<LayerConstraint> allParamConstraints,
                                         List<LayerConstraint> weightConstraints,
                                         List<LayerConstraint> biasConstraints,
                                         List<LayerConstraint> pointwiseConstraints){
        super.initializeConstraints(allParamConstraints, weightConstraints, biasConstraints);
        if(pointwiseConstraints != null){
            if(constraints == null){
                constraints = new ArrayList<>();
            }
            for (LayerConstraint constraint : pointwiseConstraints) {
                LayerConstraint clonedConstraint = constraint.clone();
                clonedConstraint.setParams(Collections.singleton(SeparableConvolutionParamInitializer.POINT_WISE_WEIGHT_KEY));
                constraints.add(clonedConstraint);
            }
        }
    }

    public boolean hasBias(){
        return hasBias;
    }

    @Override
    public SeparableConvolution2D clone() {
        SeparableConvolution2D clone = (SeparableConvolution2D) super.clone();
        if (clone.kernelSize != null)
            clone.kernelSize = clone.kernelSize.clone();
        if (clone.stride != null)
            clone.stride = clone.stride.clone();
        if (clone.padding != null)
            clone.padding = clone.padding.clone();
        return clone;
    }

    @Override
    public double getL1ByParam(String paramName) {
        switch (paramName) {
            case SeparableConvolutionParamInitializer.DEPTH_WISE_WEIGHT_KEY:
                return l1;
            case SeparableConvolutionParamInitializer.POINT_WISE_WEIGHT_KEY:
                return l1;
            case SeparableConvolutionParamInitializer.BIAS_KEY:
                return l1Bias;
            default:
                throw new IllegalArgumentException("Unknown parameter name: \"" + paramName + "\"");
        }
    }

    @Override
    public double getL2ByParam(String paramName) {
        switch (paramName) {
            case SeparableConvolutionParamInitializer.DEPTH_WISE_WEIGHT_KEY:
                return l2;
            case SeparableConvolutionParamInitializer.POINT_WISE_WEIGHT_KEY:
                return l2;
            case SeparableConvolutionParamInitializer.BIAS_KEY:
                return l2Bias;
            default:
                throw new IllegalArgumentException("Unknown parameter name: \"" + paramName + "\"");
        }
    }

    @Override
    public Layer instantiate(Collection<IterationListener> iterationListeners, String name,
                             int layerIndex, int numInputs, INDArray layerParamsView, boolean initializeParams) {
        LayerValidation.assertNInNOutSet("SeparableConvolution2D", getLayerName(), layerIndex, getNIn(), getNOut());

        org.deeplearning4j.nn.layers.convolution.SeparableConvolution2DLayer ret =
                new org.deeplearning4j.nn.layers.convolution.SeparableConvolution2DLayer(this);
        ret.setIndex(layerIndex);
        ret.setParamsViewArray(layerParamsView);
        Map<String, INDArray> paramTable = initializer().init(this, layerParamsView, initializeParams);
        ret.setParamTable(paramTable);
        ret.setConf(this);

        System.out.println(layerParamsView);
        return ret;
    }

    @Override
    public ParamInitializer initializer() {
        return SeparableConvolutionParamInitializer.getInstance();
    }

    @Override
    public InputType[] getOutputType(int layerIndex, InputType... inputType) {
        if (preProcessor != null) {
            inputType = preProcessor.getOutputType(inputType);
        }
        if (inputType == null || inputType.length != 1 || inputType[0].getType() != InputType.Type.CNN) {
            throw new IllegalStateException("Invalid input for Convolution layer (layer name=\"" + getLayerName()
                    + "\"): Expected CNN input, got " + (inputType == null ? null : Arrays.toString(inputType)));
        }

        return new InputType[]{
                InputTypeUtil.getOutputTypeCnnLayers(inputType[0], kernelSize, stride, padding, dilation,
                convolutionMode, nOut, layerIndex, getLayerName(), SeparableConvolution2DLayer.class)};
    }


    public static class Builder extends BaseConvBuilder<Builder> {

        public int depthMultiplier = 1;

        public Builder(int[] kernelSize, int[] stride, int[] padding) {
            super(kernelSize, stride, padding);
        }

        public Builder(int[] kernelSize, int[] stride) {
            super(kernelSize, stride);
        }

        public Builder(int... kernelSize) {
            super(kernelSize);
        }

        public Builder() {
            super();
        }

        /**
         * Set depth multiplier of depth-wise step in separable convolution
         *
         * @param depthMultiplier integer value, for each input map we get depthMultipler
         *                        outputs in depth-wise step.
         * @return Builder
         */
        public  Builder depthMultiplier(int depthMultiplier) {
            this.depthMultiplier = depthMultiplier;
            return this;
        }

        protected List<LayerConstraint> pointWiseConstraints;

        /**
         * Set constraints to be applied to the point-wise convolution weight parameters of this layer.
         * Default: no constraints.<br>
         * Constraints can be used to enforce certain conditions (non-negativity of parameters, max-norm regularization,
         * etc). These constraints are applied at each iteration, after the parameters have been updated.
         *
         * @param constraints Constraints to apply to the point-wise convolution parameters of this layer
         */
        public Builder constrainPointWise(LayerConstraint... constraints) {
            this.pointWiseConstraints = Arrays.asList(constraints);
            return this;
        }



        /**
         * Set the convolution mode for the Convolution layer.
         * See {@link ConvolutionMode} for more details
         *
         * @param convolutionMode Convolution mode for layer
         */
        @Override
        public Builder convolutionMode(ConvolutionMode convolutionMode) {
            this.convolutionMode = convolutionMode;
            return this;
        }

        @Override
        public Builder nIn(int nIn) {
            super.nIn(nIn);
            return this;
        }

        @Override
        public Builder nOut(int nOut) {
            super.nOut(nOut);
            return this;
        }

        /**
         * Defaults to "PREFER_FASTEST", but "NO_WORKSPACE" uses less memory.
         *
         * @param cudnnAlgoMode
         */
        @Override
        public Builder cudnnAlgoMode(AlgoMode cudnnAlgoMode) {
            super.cudnnAlgoMode(cudnnAlgoMode);
            return this;
        }

        /**
         * Layer name assigns layer string name.
         * Allows easier differentiation between layers.
         *
         * @param layerName
         */
        @Override
        public Builder name(String layerName) {
            super.name(layerName);
            return this;
        }

        @Override
        public Builder activation(IActivation activationFunction) {
            super.activation(activationFunction);
            return this;
        }

        @Override
        public Builder activation(Activation activation) {
            super.activation(activation);
            return this;
        }

        /**
         * Weight initialization scheme.
         *
         * @param weightInit
         * @see WeightInit
         */
        @Override
        public Builder weightInit(WeightInit weightInit) {
            super.weightInit(weightInit);
            return this;
        }

        @Override
        public Builder biasInit(double biasInit) {
            super.biasInit(biasInit);
            return this;
        }

        /**
         * Distribution to sample initial weights from. Used in conjunction with
         * .weightInit(WeightInit.DISTRIBUTION).
         *
         * @param dist
         */
        @Override
        public Builder dist(Distribution dist) {
            super.dist(dist);
            return this;
        }

        /**
         * L1 regularization coefficient (weights only). Use {@link #l1Bias(double)} to configure the l1 regularization
         * coefficient for the bias.
         *
         * @param l1 L1 regularization coefficient
         */
        @Override
        public Builder l1(double l1) {
            return super.l1(l1);
        }

        /**
         * L2 regularization coefficient (weights only). Use {@link #l2Bias(double)} to configure the l2 regularization
         * coefficient for the bias.
         *
         * @param l2 L2 regularization coefficient
         */
        @Override
        public Builder l2(double l2) {
            return super.l2(l2);
        }

        /**
         * L1 regularization coefficient for the bias. Default: 0. See also {@link #l1(double)}
         *
         * @param l1Bias L1 regularization coefficient (bias)
         */
        @Override
        public Builder l1Bias(double l1Bias) {
            return super.l1Bias(l1Bias);
        }

        /**
         * L2 regularization coefficient for the bias. Default: 0. See also {@link #l2(double)}
         *
         * @param l2Bias
         */
        @Override
        public Builder l2Bias(double l2Bias) {
            return super.l2Bias(l2Bias);
        }

        /**
         * Gradient updater. For example, SGD for standard stochastic gradient descent, NESTEROV for Nesterov momentum,
         * RSMPROP for RMSProp, etc.
         *
         * @param updater
         * @see Updater
         */
        @Override
        @Deprecated
        public Builder updater(Updater updater) {
            return super.updater(updater);
        }

        /**
         * Gradient normalization strategy. Used to specify gradient renormalization, gradient clipping etc.
         *
         * @param gradientNormalization Type of normalization to use. Defaults to None.
         * @see GradientNormalization
         */
        @Override
        public Builder gradientNormalization(GradientNormalization gradientNormalization) {
            super.gradientNormalization(gradientNormalization);
            return this;
        }

        /**
         * Threshold for gradient normalization, only used for GradientNormalization.ClipL2PerLayer,
         * GradientNormalization.ClipL2PerParamType, and GradientNormalization.ClipElementWiseAbsoluteValue<br>
         * Not used otherwise.<br>
         * L2 threshold for first two types of clipping, or absolute value threshold for last type of clipping.
         *
         * @param threshold
         */
        @Override
        public Builder gradientNormalizationThreshold(double threshold) {
            super.gradientNormalizationThreshold(threshold);
            return this;
        }

        /**
         * Size of the convolution
         * rows/columns
         * @param kernelSize the height and width of the
         *                   kernel
         * @return
         */
        public Builder kernelSize(int... kernelSize) {
            this.kernelSize = kernelSize;
            return this;
        }

        public Builder stride(int... stride) {
            this.stride = stride;
            return this;
        }

        public Builder padding(int... padding) {
            this.padding = padding;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public SeparableConvolution2D build() {
            ConvolutionUtils.validateCnnKernelStridePadding(kernelSize, stride, padding);

            return new SeparableConvolution2D(this);
        }
    }

}