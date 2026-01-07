import React, { useState, useMemo } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface Sprint {
  id: string;
  name: string;
  startDate: string | null;
  endDate: string | null;
  status: 'PLANNED' | 'ACTIVE' | 'COMPLETED';
}

interface SprintCalendarProps {
  selectedStartDate?: string;
  selectedEndDate?: string;
  onDateSelect: (date: string, type: 'start' | 'end') => void;
  sprints: Sprint[];
  selectingType: 'start' | 'end';
  onSelectingTypeChange: (type: 'start' | 'end') => void;
}

const SprintCalendar: React.FC<SprintCalendarProps> = ({
  selectedStartDate,
  selectedEndDate,
  onDateSelect,
  sprints,
  selectingType,
  onSelectingTypeChange
}) => {
  const [currentMonth, setCurrentMonth] = useState(new Date());

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  // Generate calendar days
  const calendarDays = useMemo(() => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());
    
    const days = [];
    const current = new Date(startDate);
    
    while (current <= lastDay || current.getDay() !== 0) {
      days.push(new Date(current));
      current.setDate(current.getDate() + 1);
      if (days.length > 42) break; // Max 6 weeks
    }
    
    return days;
  }, [currentMonth]);

  // Get sprints for a specific date
  const getSprintsForDate = (date: Date) => {
    const dateStr = date.toISOString().split('T')[0];
    return sprints.filter(sprint => {
      // Skip sprints without dates
      if (!sprint.startDate || !sprint.endDate) return false;
      
      const sprintStart = new Date(sprint.startDate);
      const sprintEnd = new Date(sprint.endDate);
      const currentDate = new Date(dateStr);
      return currentDate >= sprintStart && currentDate <= sprintEnd;
    });
  };

  // Check if date is in current month
  const isCurrentMonth = (date: Date) => {
    return date.getMonth() === currentMonth.getMonth();
  };

  // Check if date is selected
  const isSelected = (date: Date) => {
    const dateStr = date.toISOString().split('T')[0];
    return dateStr === selectedStartDate || dateStr === selectedEndDate;
  };

  // Check if date is in selected range
  const isInRange = (date: Date) => {
    if (!selectedStartDate || !selectedEndDate) return false;
    const dateStr = date.toISOString().split('T')[0];
    return dateStr > selectedStartDate && dateStr < selectedEndDate;
  };

  // Check if date is today
  const isToday = (date: Date) => {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  };

  // Handle date click
  const handleDateClick = (date: Date) => {
    const dateStr = date.toISOString().split('T')[0];
    onDateSelect(dateStr, selectingType);
  };

  // Navigate months
  const previousMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1));
  };

  const nextMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1));
  };

  // Get sprint color based on status
  const getSprintColor = (status: Sprint['status']) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-500/20 border-green-500/30 text-green-700 dark:text-green-300';
      case 'PLANNED':
        return 'bg-blue-500/20 border-blue-500/30 text-blue-700 dark:text-blue-300';
      case 'COMPLETED':
        return 'bg-gray-500/20 border-gray-500/30 text-gray-700 dark:text-gray-300';
      default:
        return 'bg-gray-500/20 border-gray-500/30 text-gray-700 dark:text-gray-300';
    }
  };

  return (
    <div className="bg-white/50 dark:bg-[#1e293b]/50 backdrop-blur-md rounded-2xl border border-white/20 dark:border-white/10 p-4 shadow-xl flex flex-col max-h-[500px]">
      {/* Header */}
      <div className="flex items-center justify-between mb-4 flex-shrink-0">
        <button
          onClick={previousMonth}
          className="p-2 rounded-xl bg-white/50 dark:bg-white/10 hover:bg-white/70 dark:hover:bg-white/20 transition-colors"
        >
          <ChevronLeft className="w-4 h-4 text-gray-600 dark:text-gray-300" />
        </button>
        
        <h3 className="text-base font-bold text-gray-800 dark:text-white">
          {monthNames[currentMonth.getMonth()]} {currentMonth.getFullYear()}
        </h3>
        
        <button
          onClick={nextMonth}
          className="p-2 rounded-xl bg-white/50 dark:bg-white/10 hover:bg-white/70 dark:hover:bg-white/20 transition-colors"
        >
          <ChevronRight className="w-4 h-4 text-gray-600 dark:text-gray-300" />
        </button>
      </div>

      {/* Selection Type Toggle */}
      <div className="flex gap-2 mb-4 flex-shrink-0">
        <button
          onClick={() => onSelectingTypeChange('start')}
          className={`flex-1 px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
            selectingType === 'start'
              ? 'bg-blue-500/20 text-blue-700 dark:text-blue-300 border border-blue-500/30'
              : 'bg-white/30 dark:bg-white/10 text-gray-600 dark:text-gray-400 hover:bg-white/50 dark:hover:bg-white/20'
          }`}
        >
          Select Start Date
        </button>
        <button
          onClick={() => onSelectingTypeChange('end')}
          className={`flex-1 px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
            selectingType === 'end'
              ? 'bg-blue-500/20 text-blue-700 dark:text-blue-300 border border-blue-500/30'
              : 'bg-white/30 dark:bg-white/10 text-gray-600 dark:text-gray-400 hover:bg-white/50 dark:hover:bg-white/20'
          }`}
        >
          Select End Date
        </button>
      </div>

      {/* Day headers */}
      <div className="grid grid-cols-7 gap-1 mb-2 flex-shrink-0">
        {dayNames.map(day => (
          <div key={day} className="p-1 text-center text-xs font-medium text-gray-500 dark:text-gray-400">
            {day}
          </div>
        ))}
      </div>

      {/* Calendar grid */}
      <div className="grid grid-cols-7 gap-1 flex-1 min-h-0 overflow-hidden">
        {calendarDays.map((date, index) => {
          const daySprintss = getSprintsForDate(date);
          const isCurrentMonthDay = isCurrentMonth(date);
          const isSelectedDay = isSelected(date);
          const isInRangeDay = isInRange(date);
          const isTodayDay = isToday(date);

          return (
            <div key={index} className="relative">
              <button
                onClick={() => handleDateClick(date)}
                disabled={!isCurrentMonthDay}
                className={`
                  w-full aspect-square p-1 rounded-lg text-xs font-medium transition-all relative overflow-hidden
                  ${!isCurrentMonthDay 
                    ? 'text-gray-300 dark:text-gray-600 cursor-not-allowed' 
                    : 'hover:bg-white/50 dark:hover:bg-white/10 cursor-pointer'
                  }
                  ${isSelectedDay 
                    ? 'bg-blue-500/30 text-blue-800 dark:text-blue-200 border border-blue-500/50' 
                    : ''
                  }
                  ${isInRangeDay 
                    ? 'bg-blue-500/10 text-blue-700 dark:text-blue-300' 
                    : ''
                  }
                  ${isTodayDay && !isSelectedDay 
                    ? 'bg-orange-500/20 text-orange-700 dark:text-orange-300 border border-orange-500/30' 
                    : ''
                  }
                `}
              >
                <span className="relative z-10">{date.getDate()}</span>
                
                {/* Sprint indicators */}
                {daySprintss.length > 0 && (
                  <div className="absolute inset-0 flex flex-col justify-end p-0.5 z-0">
                    {daySprintss.slice(0, 2).map((sprint) => (
                      <div
                        key={sprint.id}
                        className={`
                          h-0.5 rounded-full mb-0.5 border ${getSprintColor(sprint.status)}
                        `}
                        title={`${sprint.name} (${sprint.status})`}
                      />
                    ))}
                    {daySprintss.length > 2 && (
                      <div className="h-0.5 bg-gray-400/30 rounded-full" title={`+${daySprintss.length - 2} more sprints`} />
                    )}
                  </div>
                )}
              </button>
            </div>
          );
        })}
      </div>

      {/* Legend */}
      <div className="mt-3 pt-3 border-t border-gray-200 dark:border-white/10 flex-shrink-0">
        <div className="flex flex-wrap gap-3 text-xs">
          <div className="flex items-center gap-1.5">
            <div className="w-3 h-0.5 bg-green-500/30 border border-green-500/50 rounded-full"></div>
            <span className="text-gray-600 dark:text-gray-400">Active</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-3 h-0.5 bg-blue-500/30 border border-blue-500/50 rounded-full"></div>
            <span className="text-gray-600 dark:text-gray-400">Planned</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-3 h-0.5 bg-gray-500/30 border border-gray-500/50 rounded-full"></div>
            <span className="text-gray-600 dark:text-gray-400">Completed</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SprintCalendar;