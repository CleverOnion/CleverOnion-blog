const AdminDashboard = () => {
  return (
    <div className="h-full p-6 overflow-y-auto">
      <div className="space-y-6">
      {/* 统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Sales Card */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-medium text-gray-600">Sales</h3>
            <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
              <svg className="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
              </svg>
            </div>
          </div>
          <p className="text-2xl font-bold text-gray-900 mb-1">AED 1000.00</p>
          <div className="flex items-center text-sm">
            <span className="text-green-600 font-medium">+12%</span>
            <span className="text-gray-500 ml-1">from last month</span>
          </div>
        </div>

        {/* Visits Card */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-medium text-gray-600">Visits</h3>
            <div className="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
              <svg className="w-4 h-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
            </div>
          </div>
          <p className="text-2xl font-bold text-gray-900 mb-1">100</p>
          <div className="flex items-center text-sm">
            <span className="text-gray-500">Appointments</span>
          </div>
        </div>

        {/* Appointments Card */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-medium text-gray-600">Appointments</h3>
            <div className="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
              <svg className="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
          <p className="text-2xl font-bold text-gray-900 mb-1">324</p>
          <div className="flex items-center text-sm">
            <span className="text-red-600 font-medium">-5%</span>
            <span className="text-gray-500 ml-1">from last week</span>
          </div>
        </div>

        {/* Revenue Card */}
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-medium text-gray-600">Revenue</h3>
            <div className="w-8 h-8 bg-yellow-100 rounded-lg flex items-center justify-center">
              <svg className="w-4 h-4 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
              </svg>
            </div>
          </div>
          <p className="text-2xl font-bold text-gray-900 mb-1">AED 12.5K</p>
          <div className="flex items-center text-sm">
            <span className="text-green-600 font-medium">+8%</span>
            <span className="text-gray-500 ml-1">from last month</span>
          </div>
        </div>
      </div>
      
      {/* 主要内容区域 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 左侧：统计图表和进度 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 统计进度卡片 */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
              <h3 className="text-sm font-medium text-gray-600 mb-4">Appointment Completed</h3>
              <div className="flex items-center justify-center mb-4">
                <div className="relative w-20 h-20">
                  <svg className="w-20 h-20 transform -rotate-90" viewBox="0 0 36 36">
                    <path className="text-gray-200" stroke="currentColor" strokeWidth="3" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                    <path className="text-blue-600" stroke="currentColor" strokeWidth="3" strokeDasharray="40, 100" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                  </svg>
                  <div className="absolute inset-0 flex items-center justify-center">
                    <span className="text-lg font-bold text-gray-900">20</span>
                  </div>
                </div>
              </div>
              <p className="text-center text-sm text-gray-500">/50</p>
            </div>

            <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
              <h3 className="text-sm font-medium text-gray-600 mb-4">Online Customers</h3>
              <div className="flex items-center justify-center mb-4">
                <div className="relative w-20 h-20">
                  <svg className="w-20 h-20 transform -rotate-90" viewBox="0 0 36 36">
                    <path className="text-gray-200" stroke="currentColor" strokeWidth="3" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                    <path className="text-purple-600" stroke="currentColor" strokeWidth="3" strokeDasharray="20, 100" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                  </svg>
                  <div className="absolute inset-0 flex items-center justify-center">
                    <span className="text-lg font-bold text-gray-900">10</span>
                  </div>
                </div>
              </div>
              <p className="text-center text-sm text-gray-500">/105</p>
            </div>

            <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
              <h3 className="text-sm font-medium text-gray-600 mb-4">New Customers</h3>
              <div className="flex items-center justify-center mb-4">
                <div className="relative w-20 h-20">
                  <svg className="w-20 h-20 transform -rotate-90" viewBox="0 0 36 36">
                    <path className="text-gray-200" stroke="currentColor" strokeWidth="3" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                    <path className="text-green-600" stroke="currentColor" strokeWidth="3" strokeDasharray="5, 100" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                  </svg>
                  <div className="absolute inset-0 flex items-center justify-center">
                    <span className="text-lg font-bold text-gray-900">5</span>
                  </div>
                </div>
              </div>
              <p className="text-center text-sm text-gray-500">/100</p>
            </div>
          </div>

          {/* 交易历史表格 */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100">
            <div className="p-6 border-b border-gray-100">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold text-gray-900">Translation History</h3>
                <button className="text-sm text-blue-600 hover:text-blue-700">View All</button>
              </div>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Customer</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">Guy Webb</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">Service</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">13 Dec 2020</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">AED 120</td>
                  </tr>
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">Tini-Mose</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">Service</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">13 Dec 2020</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">AED 120</td>
                  </tr>
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">Saniya-V</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">Service</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">13 Dec 2020</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">AED 120</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* 右侧：预约列表 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100">
          <div className="p-6 border-b border-gray-100">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">Appointments</h3>
              <button className="w-8 h-8 bg-blue-600 text-white rounded-lg flex items-center justify-center hover:bg-blue-700">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
              </button>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Today</span>
              <div className="flex items-center space-x-2">
                <button className="p-1 hover:bg-gray-100 rounded">
                  <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                  </svg>
                </button>
                <span className="text-gray-900 font-medium">28 November</span>
                <button className="p-1 hover:bg-gray-100 rounded">
                  <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
          
          <div className="p-4">
            {/* 搜索框 */}
            <div className="relative mb-4">
              <input
                type="text"
                placeholder="Search Customer"
                className="w-full pl-10 pr-4 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <svg className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>

            {/* 预约列表 */}
            <div className="space-y-3">
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                <div className="flex justify-between items-start mb-2">
                  <span className="text-sm font-medium text-gray-900">10:30 PM</span>
                  <span className="text-xs text-red-600 bg-red-100 px-2 py-1 rounded">Canceled</span>
                </div>
                <p className="text-sm text-gray-600 mb-1">Mohammad Ibrahim</p>
                <p className="text-xs text-gray-500">AED 120</p>
              </div>

              <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg">
                <div className="flex justify-between items-start mb-2">
                  <span className="text-sm font-medium text-gray-900">11:30 AM</span>
                  <span className="text-xs text-blue-600">Hair cut and 2 more</span>
                </div>
                <p className="text-sm text-gray-600 mb-1">Mina Moradi</p>
                <p className="text-xs text-gray-500">AED 120</p>
              </div>

              <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg">
                <div className="flex justify-between items-start mb-2">
                  <span className="text-sm font-medium text-gray-900">9:30 AM</span>
                  <span className="text-xs text-blue-600">Hair cut</span>
                </div>
                <p className="text-sm text-gray-600 mb-1">Mohammad Mazhar</p>
                <p className="text-xs text-gray-500">AED 120</p>
              </div>

              <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg">
                <div className="flex justify-between items-start mb-2">
                  <span className="text-sm font-medium text-gray-900">8:30 AM</span>
                  <span className="text-xs text-gray-600">No Show</span>
                </div>
                <p className="text-sm text-gray-600 mb-1">Shruthi grv</p>
                <p className="text-xs text-gray-500">AED 120</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>
  );
};

export default AdminDashboard;