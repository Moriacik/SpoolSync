#!/bin/bash

# SpoolSync Web - Development Setup Script
# This script helps you set up the development environment

echo "🚀 SpoolSync Web - Development Setup"
echo "====================================="
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js from https://nodejs.org/"
    exit 1
fi

echo "✓ Node.js version: $(node --version)"
echo ""

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed."
    exit 1
fi

echo "✓ npm version: $(npm --version)"
echo ""

# Install dependencies
echo "📦 Installing dependencies..."
npm install

if [ $? -ne 0 ]; then
    echo "❌ Failed to install dependencies"
    exit 1
fi

echo "✓ Dependencies installed"
echo ""

# Check if .env.local exists
if [ ! -f .env.local ]; then
    echo "📝 Creating .env.local file..."
    if [ -f .env.example ]; then
        cp .env.example .env.local
        echo "✓ .env.local created from .env.example"
        echo "⚠️  Please edit .env.local and add your Firebase credentials"
    else
        echo "⚠️  .env.example not found. Please create .env.local manually"
    fi
else
    echo "✓ .env.local already exists"
fi

echo ""
echo "✅ Setup complete!"
echo ""
echo "📚 Next steps:"
echo "1. Edit .env.local with your Firebase credentials"
echo "2. Run: npm run dev"
echo "3. Open: http://localhost:5173/"
echo ""
echo "📖 For detailed setup guide, see: SETUP.md"
