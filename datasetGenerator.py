import random
import nltk

nltk.download("words", quiet=True)
from nltk.corpus import words

def generate_random_words(num_words, word_list):
    return " ".join(random.choice(word_list) for _ in range(num_words))

def create_documents(num_docs, num_words, output_file):
    word_list = words.words()  # huge list of English words

    with open(output_file, "w", encoding="utf-8") as f:
        for i in range(1, num_docs + 1):
            random_words = generate_random_words(num_words, word_list)
            f.write(f"Document{i} {random_words}\n")

if __name__ == "__main__":
    N = 5000
    num_docs = 3
    output_file = "/workspaces/CCDAAssignment2/shared-folder/input/data/inputTest3.txt"
    
    create_documents(num_docs, N, output_file)
    print(f"File '{output_file}' created with {num_docs} documents.")