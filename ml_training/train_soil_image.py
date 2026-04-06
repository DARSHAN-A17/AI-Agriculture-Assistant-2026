"""
Soil Image Classification Model Training Script
================================================
Trains a CNN model using MobileNetV2 transfer learning for soil type classification.

Dataset: https://www.kaggle.com/datasets/msambare/soil-classification
Download the dataset and extract to a folder, then run this script.

Usage:
    python train_soil_image.py --dataset_path /path/to/SoilDataset --epochs 20 --batch_size 32

Output:
    - soil_image_model.h5 (Keras model)
    - soil_image_model.tflite (TensorFlow Lite model)
    - soil_labels.txt (class labels)
    - soil_training_history.png (accuracy/loss plots)
"""

import argparse
import os
import numpy as np
import matplotlib.pyplot as plt
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.preprocessing.image import ImageDataGenerator
import kaggle_utils


def create_model(num_classes, input_shape=(224, 224, 3)):
    """Create soil classification CNN model."""
    base_model = MobileNetV2(
        weights='imagenet',
        include_top=False,
        input_shape=input_shape
    )

    base_model.trainable = False

    model = keras.Sequential([
        base_model,
        layers.GlobalAveragePooling2D(),
        layers.BatchNormalization(),
        layers.Dropout(0.4),
        layers.Dense(128, activation='relu'),
        layers.BatchNormalization(),
        layers.Dropout(0.3),
        layers.Dense(num_classes, activation='softmax')
    ])

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=0.001),
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )

    return model, base_model


def train(args):
    """Main training function."""
    print(f"Loading soil dataset from: {args.dataset_path}")

    # Check/Download dataset
    if args.download or not os.path.exists(args.dataset_path):
        dataset_slug = "vuppalaadithyasairam/soil-classification-image-data"
        print(f"🔍 Dataset not found or download requested. Attempting to download '{dataset_slug}'...")
        success = kaggle_utils.download_dataset(dataset_slug, args.dataset_path)
        if not success:
            print("❌ Failure during dataset preparation. Please check your Kaggle API setup.")
            return

    # Data augmentation
    train_datagen = ImageDataGenerator(
        rescale=1.0 / 255,
        rotation_range=40,
        width_shift_range=0.2,
        height_shift_range=0.2,
        shear_range=0.2,
        zoom_range=0.3,
        horizontal_flip=True,
        vertical_flip=True,
        brightness_range=[0.8, 1.2],
        fill_mode='nearest',
        validation_split=0.2
    )

    val_datagen = ImageDataGenerator(
        rescale=1.0 / 255,
        validation_split=0.2
    )

    IMG_SIZE = (224, 224)

    train_generator = train_datagen.flow_from_directory(
        args.dataset_path,
        target_size=IMG_SIZE,
        batch_size=args.batch_size,
        class_mode='categorical',
        subset='training',
        shuffle=True
    )

    val_generator = val_datagen.flow_from_directory(
        args.dataset_path,
        target_size=IMG_SIZE,
        batch_size=args.batch_size,
        class_mode='categorical',
        subset='validation',
        shuffle=False
    )

    num_classes = train_generator.num_classes
    class_names = list(train_generator.class_indices.keys())

    print(f"\nSoil types found: {num_classes}")
    print(f"Classes: {class_names}")
    print(f"Training samples: {train_generator.samples}")
    print(f"Validation samples: {val_generator.samples}")

    model, base_model = create_model(num_classes)
    model.summary()

    callbacks = [
        keras.callbacks.EarlyStopping(
            monitor='val_accuracy',
            patience=5,
            restore_best_weights=True
        ),
        keras.callbacks.ReduceLROnPlateau(
            monitor='val_loss',
            factor=0.5,
            patience=3,
            min_lr=1e-7
        ),
        keras.callbacks.ModelCheckpoint(
            'soil_image_best.h5',
            monitor='val_accuracy',
            save_best_only=True,
            verbose=1
        )
    ]

    # Phase 1: Feature extraction
    print("\n=== Phase 1: Feature Extraction ===")
    history1 = model.fit(
        train_generator,
        validation_data=val_generator,
        epochs=args.epochs,
        callbacks=callbacks,
        verbose=1
    )

    # Phase 2: Fine-tune
    print("\n=== Phase 2: Fine-Tuning ===")
    base_model.trainable = True
    for layer in base_model.layers[:100]:
        layer.trainable = False

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=1e-5),
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )

    history2 = model.fit(
        train_generator,
        validation_data=val_generator,
        epochs=args.fine_tune_epochs,
        callbacks=callbacks,
        verbose=1
    )

    # Save
    output_dir = args.output_dir
    os.makedirs(output_dir, exist_ok=True)

    model_path = os.path.join(output_dir, 'soil_image_model.h5')
    model.save(model_path)
    print(f"\nModel saved to: {model_path}")

    labels_path = os.path.join(output_dir, 'soil_labels.txt')
    with open(labels_path, 'w') as f:
        for name in class_names:
            f.write(name + '\n')
    print(f"Labels saved to: {labels_path}")

    # Convert to TFLite
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    tflite_path = os.path.join(output_dir, 'soil_image_model.tflite')
    with open(tflite_path, 'wb') as f:
        f.write(tflite_model)
    print(f"TFLite model saved to: {tflite_path}")
    print(f"TFLite model size: {len(tflite_model) / (1024 * 1024):.1f} MB")

    # Plot
    acc = history1.history['accuracy'] + history2.history['accuracy']
    val_acc = history1.history['val_accuracy'] + history2.history['val_accuracy']
    loss = history1.history['loss'] + history2.history['loss']
    val_loss = history1.history['val_loss'] + history2.history['val_loss']

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 5))
    ax1.plot(acc, label='Train Acc')
    ax1.plot(val_acc, label='Val Acc')
    ax1.set_title('Accuracy')
    ax1.legend()
    ax1.grid(True)
    ax2.plot(loss, label='Train Loss')
    ax2.plot(val_loss, label='Val Loss')
    ax2.set_title('Loss')
    ax2.legend()
    ax2.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'soil_training_history.png'), dpi=150)

    print("\n=== Final Evaluation ===")
    loss, acc = model.evaluate(val_generator)
    print(f"Validation Accuracy: {acc * 100:.2f}%")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Train Soil Image Classification Model')
    parser.add_argument('--dataset_path', type=str, required=True,
                        help='Path to soil classification dataset')
    parser.add_argument('--epochs', type=int, default=15)
    parser.add_argument('--fine_tune_epochs', type=int, default=10)
    parser.add_argument('--batch_size', type=int, default=32)
    parser.add_argument('--output_dir', type=str, default='./output')
    parser.add_argument('--download', action='store_true', help='Download dataset from Kaggle if missing')
    args = parser.parse_args()
    train(args)
