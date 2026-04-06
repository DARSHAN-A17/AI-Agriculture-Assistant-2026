"""
TFLite Model Conversion Utility
================================
Converts trained Keras models (.h5) to TensorFlow Lite (.tflite) format.

Usage:
    python convert_to_tflite.py --model_path model.h5 --output_path model.tflite

Options:
    --quantize    Apply post-training quantization for smaller model size
"""

import argparse
import os
import tensorflow as tf


def convert(args):
    """Convert Keras model to TFLite."""
    print(f"Loading model: {args.model_path}")
    model = tf.keras.models.load_model(args.model_path)
    model.summary()

    converter = tf.lite.TFLiteConverter.from_keras_model(model)

    if args.quantize:
        print("Applying post-training quantization...")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]

        # Optional: full integer quantization
        if args.full_integer:
            print("Full integer quantization enabled.")
            converter.target_spec.supported_types = [tf.int8]

    tflite_model = converter.convert()

    # Save
    with open(args.output_path, 'wb') as f:
        f.write(tflite_model)

    original_size = os.path.getsize(args.model_path)
    tflite_size = len(tflite_model)

    print(f"\n✅ Conversion complete!")
    print(f"Original model: {original_size / (1024 * 1024):.1f} MB")
    print(f"TFLite model:   {tflite_size / (1024 * 1024):.1f} MB")
    print(f"Compression:    {(1 - tflite_size / original_size) * 100:.1f}%")
    print(f"Output: {args.output_path}")

    # Verify the converted model
    print("\nVerifying converted model...")
    interpreter = tf.lite.Interpreter(model_content=tflite_model)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    print(f"Input shape:  {input_details[0]['shape']}")
    print(f"Input dtype:  {input_details[0]['dtype']}")
    print(f"Output shape: {output_details[0]['shape']}")
    print(f"Output dtype: {output_details[0]['dtype']}")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Convert Keras model to TFLite')
    parser.add_argument('--model_path', type=str, required=True,
                        help='Path to .h5 Keras model')
    parser.add_argument('--output_path', type=str, required=True,
                        help='Output path for .tflite file')
    parser.add_argument('--quantize', action='store_true',
                        help='Apply post-training quantization')
    parser.add_argument('--full_integer', action='store_true',
                        help='Apply full integer quantization (requires --quantize)')
    args = parser.parse_args()
    convert(args)
