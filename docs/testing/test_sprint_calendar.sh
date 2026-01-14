#!/bin/bash

echo "🔍 Testing Sprint Calendar Integration..."
echo ""

# Check if SprintCalendar component exists
if [ -f "frontend/src/components/ui/SprintCalendar.tsx" ]; then
    echo "✅ SprintCalendar component exists"
else
    echo "❌ SprintCalendar component missing"
    exit 1
fi

# Check if SprintCalendar is imported in App.tsx
if grep -q "import SprintCalendar" frontend/src/App.tsx; then
    echo "✅ SprintCalendar is imported in App.tsx"
else
    echo "❌ SprintCalendar import missing in App.tsx"
    exit 1
fi

# Check if SprintCalendar is used in create modal
if grep -q "<SprintCalendar" frontend/src/App.tsx; then
    echo "✅ SprintCalendar is used in modals"
else
    echo "❌ SprintCalendar usage missing in modals"
    exit 1
fi

# Check if both frontend and backend are running
if curl -s http://localhost:3001 > /dev/null; then
    echo "✅ Frontend is running on port 3001"
else
    echo "❌ Frontend is not running"
fi

if curl -s http://localhost:8080/api/sprints > /dev/null; then
    echo "✅ Backend is running on port 8080"
else
    echo "❌ Backend is not running"
fi

echo ""
echo "🎉 Sprint Calendar Integration Test Complete!"
echo "📱 You can now test the custom calendar at: http://localhost:3001"
echo ""
echo "To test the calendar:"
echo "1. Go to Sprints section"
echo "2. Click 'Plan New Sprint' or edit an existing sprint"
echo "3. Verify the custom calendar shows existing sprints as colored bars"
echo "4. Test date selection with the toggle buttons"