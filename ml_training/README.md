# ML Training Scripts for AI Smart Agriculture Assistant

This directory contains **Google Colab notebooks** and Python scripts for training all three ML models.

## 🚀 Quick Start with Google Colab (Recommended)

Upload these notebooks to [Google Colab](https://colab.research.google.com/) and run them:

| Notebook | Model | GPU? | Time |
|----------|-------|------|------|
| `Plant_Disease_Training.ipynb` | CNN (MobileNetV2) — 38 disease classes | ✅ Required | ~1-2 hours |
| `Soil_Image_Training.ipynb` | CNN (MobileNetV2) — 5 soil types | ✅ Required | ~30-60 min |
| `Soil_Nutrient_Training.ipynb` | Random Forest — crop recommendation | ❌ CPU OK | ~1 min |

### How to Use:
1. Go to https://colab.research.google.com/
2. Click **File → Upload notebook** → select the `.ipynb` file
3. Enable GPU: **Runtime → Change runtime type → T4 GPU**
4. Get your Kaggle API key from https://www.kaggle.com/settings → Create New Token
5. Run all cells (Shift+Enter or Runtime → Run all)
6. The trained `.tflite` model will auto-download when done

### After Training:
Copy the downloaded files to the Android app:
```
app/src/main/assets/
├── plant_disease_model.tflite    ← from Plant_Disease_Training
├── soil_image_model.tflite       ← from Soil_Image_Training
├── plant_labels.txt              ← already included
└── soil_labels.txt               ← already included
```

---

## Alternative: Local Training

If you prefer training locally:

```bash
pip install -r requirements.txt

python train_plant_disease.py --dataset_path /path/to/PlantVillage --output_dir ./output
python train_soil_image.py --dataset_path /path/to/SoilDataset --output_dir ./output
python train_soil_nutrient.py --dataset_path /path/to/Crop_recommendation.csv --output_dir ./output
```

## Dataset Sources

- **Plant Disease:** https://www.kaggle.com/datasets/emmarex/plantdisease
- **Soil Images:** https://www.kaggle.com/datasets/msambare/soil-classification
- **Crop Recommendation:** https://www.kaggle.com/datasets/atharvaingle/crop-recommendation-dataset
