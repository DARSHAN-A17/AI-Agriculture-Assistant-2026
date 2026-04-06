"""
Soil Nutrient Analysis Model Training Script
=============================================
Trains a Random Forest classifier for crop recommendation based on soil nutrients.

Dataset: https://www.kaggle.com/datasets/atharvaingle/crop-recommendation-dataset
Download the CSV file and run this script.

Usage:
    python train_soil_nutrient.py --dataset_path /path/to/Crop_recommendation.csv

Output:
    - soil_nutrient_model.pkl (Scikit-learn model)
    - crop_labels.txt (crop class labels)
    - feature_importance.png (feature importance chart)
    - classification_report.txt (detailed metrics)
"""

import argparse
import os
import json
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
import pickle
import kaggle_utils


def train(args):
    """Main training function."""
    print(f"Loading dataset from: {args.dataset_path}")

    # Check/Download dataset
    if args.download or not os.path.exists(args.dataset_path):
        dataset_slug = "atharvaingle/crop-recommendation-dataset"
        download_dir = os.path.dirname(args.dataset_path) if os.path.dirname(args.dataset_path) else "."
        print(f"🔍 Dataset not found or download requested. Attempting to download '{dataset_slug}'...")
        success = kaggle_utils.download_dataset(dataset_slug, download_dir)
        if not success:
            print("❌ Failure during dataset preparation. Please check your Kaggle API setup.")
            return

    # Load dataset
    df = pd.read_csv(args.dataset_path)
    print(f"\nDataset shape: {df.shape}")
    print(f"\nColumns: {list(df.columns)}")
    print(f"\nSample data:\n{df.head()}")
    print(f"\nCrop distribution:\n{df['label'].value_counts()}")

    # Features and labels
    feature_cols = ['N', 'P', 'K', 'temperature', 'humidity', 'ph', 'rainfall']
    X = df[feature_cols].values
    y = df['label'].values

    # Encode labels
    label_encoder = LabelEncoder()
    y_encoded = label_encoder.fit_transform(y)
    class_names = list(label_encoder.classes_)
    num_classes = len(class_names)

    print(f"\nNumber of crops: {num_classes}")
    print(f"Crop labels: {class_names}")

    # Scale features
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # Split data
    X_train, X_test, y_train, y_test = train_test_split(
        X_scaled, y_encoded, test_size=0.2, random_state=42, stratify=y_encoded
    )

    print(f"\nTraining samples: {len(X_train)}")
    print(f"Testing samples: {len(X_test)}")

    # Train Random Forest
    print("\n=== Training Random Forest Classifier ===")
    model = RandomForestClassifier(
        n_estimators=200,
        max_depth=20,
        min_samples_split=5,
        min_samples_leaf=2,
        random_state=42,
        n_jobs=-1,
        verbose=1
    )

    model.fit(X_train, y_train)

    # Evaluate
    y_pred = model.predict(X_test)
    accuracy = accuracy_score(y_test, y_pred)
    print(f"\n=== Test Accuracy: {accuracy * 100:.2f}% ===")

    # Cross-validation
    cv_scores = cross_val_score(model, X_scaled, y_encoded, cv=5, scoring='accuracy')
    print(f"Cross-validation accuracy: {cv_scores.mean() * 100:.2f}% (+/- {cv_scores.std() * 100:.2f}%)")

    # Classification report
    report = classification_report(y_test, y_pred, target_names=class_names)
    print(f"\nClassification Report:\n{report}")

    # Save outputs
    output_dir = args.output_dir
    os.makedirs(output_dir, exist_ok=True)

    # Save model
    model_path = os.path.join(output_dir, 'soil_nutrient_model.pkl')
    with open(model_path, 'wb') as f:
        pickle.dump({
            'model': model,
            'scaler': scaler,
            'label_encoder': label_encoder,
            'feature_names': feature_cols
        }, f)
    print(f"\nModel saved to: {model_path}")

    # Save labels
    labels_path = os.path.join(output_dir, 'crop_labels.txt')
    with open(labels_path, 'w') as f:
        for name in class_names:
            f.write(name + '\n')
    print(f"Labels saved to: {labels_path}")

    # Save classification report
    report_path = os.path.join(output_dir, 'classification_report.txt')
    with open(report_path, 'w') as f:
        f.write(f"Test Accuracy: {accuracy * 100:.2f}%\n")
        f.write(f"CV Accuracy: {cv_scores.mean() * 100:.2f}% (+/- {cv_scores.std() * 100:.2f}%)\n\n")
        f.write(report)
    print(f"Report saved to: {report_path}")

    # Save decision rules as JSON for Android app integration
    rules = generate_decision_rules(model, feature_cols, class_names, X_train_mean=scaler.mean_, X_train_std=scaler.scale_)
    rules_path = os.path.join(output_dir, 'decision_rules.json')
    with open(rules_path, 'w') as f:
        json.dump(rules, f, indent=2)
    print(f"Decision rules saved to: {rules_path}")

    # Feature importance plot
    plot_feature_importance(model, feature_cols, output_dir)

    print("\n✅ Training complete!")
    print(f"All outputs saved to: {output_dir}")


def generate_decision_rules(model, feature_names, class_names, X_train_mean, X_train_std):
    """Generate simplified decision rules for embedded use."""
    importances = model.feature_importances_
    rules = {
        'feature_names': feature_names,
        'class_names': class_names,
        'feature_importances': importances.tolist(),
        'scaler_mean': X_train_mean.tolist(),
        'scaler_std': X_train_std.tolist(),
        'model_type': 'RandomForest',
        'n_estimators': model.n_estimators,
        'note': 'Use the Android SoilNutrientAnalyzer for on-device inference. This file contains reference data for validation.'
    }
    return rules


def plot_feature_importance(model, feature_names, output_dir):
    """Plot and save feature importance."""
    importances = model.feature_importances_
    indices = np.argsort(importances)[::-1]

    plt.figure(figsize=(10, 6))
    plt.title('Feature Importance for Crop Recommendation')
    plt.bar(range(len(feature_names)), importances[indices], align='center', color='#4CAF50')
    plt.xticks(range(len(feature_names)), [feature_names[i] for i in indices], rotation=45)
    plt.ylabel('Importance Score')
    plt.xlabel('Soil Parameters')
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'feature_importance.png'), dpi=150)
    print("Feature importance plot saved.")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Train Soil Nutrient Analysis Model')
    parser.add_argument('--dataset_path', type=str, required=True,
                        help='Path to Crop_recommendation.csv')
    parser.add_argument('--output_dir', type=str, default='./output')
    parser.add_argument('--download', action='store_true', help='Download dataset from Kaggle')
    args = parser.parse_args()
    train(args)
