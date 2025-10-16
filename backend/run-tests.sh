 #!/bin/bash

# Script to build application with tests
echo "🧪 Building Cibaria with Tests"
echo "=============================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Build with tests (tests run during build phase)
echo -e "${YELLOW}🏗️  Building application with tests...${NC}"
docker-compose build

# Check exit code
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Build and tests completed successfully!${NC}"
else
    echo -e "${RED}❌ Build or tests failed. Check the output above.${NC}"
    exit 1
fi

echo -e "${GREEN}🎉 Ready to run with: docker compose up${NC}"