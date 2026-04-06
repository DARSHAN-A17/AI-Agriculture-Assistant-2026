import os
import zipfile
from kaggle.api.kaggle_api_extended import KaggleApi

def download_dataset(dataset_name, download_path):
    """
    Downloads and extracts a Kaggle dataset.
    
    Args:
        dataset_name (str): The Kaggle dataset identifier (e.g., 'username/dataset').
        download_path (str): The directory where the dataset should be extracted.
    """
    print(f"Initializing Kaggle API for dataset: {dataset_name}")
    
    api = KaggleApi()
    try:
        api.authenticate()
    except Exception as e:
        print(f"Kaggle Authentication Failed: {e}")
        print("Ensure you have 'kaggle.json' in ~/.kaggle/ or set KAGGLE_USERNAME and KAGGLE_KEY environment variables.")
        return False

    if not os.path.exists(download_path):
        os.makedirs(download_path)

    print(f"Downloading dataset to {download_path}...")
    try:
        api.dataset_download_files(dataset_name, path=download_path, unzip=True)
        print(f"Dataset downloaded and extracted to: {download_path}")
        return True
    except Exception as e:
        print(f"Failed to download dataset: {e}")
        return False

if __name__ == "__main__":
    # Test call
    import sys
    if len(sys.argv) > 2:
        download_dataset(sys.argv[1], sys.argv[2])
    else:
        print("Usage: python kaggle_utils.py <dataset_name> <download_path>")
