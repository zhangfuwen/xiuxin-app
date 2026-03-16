import SwiftUI

struct ContentView: View {
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            BreathingView()
                .tabItem {
                    Label("呼吸", systemImage: "lungs")
                }
                .tag(0)
            
            MeditationView()
                .tabItem {
                    Label("冥想", systemImage: "timer")
                }
                .tag(1)
            
            PracticeView()
                .tabItem {
                    Label("功课", systemImage: "checkmark.circle")
                }
                .tag(2)
            
            StatsView()
                .tabItem {
                    Label("统计", systemImage: "chart.bar")
                }
                .tag(3)
        }
    }
}

// 呼吸练习视图
struct BreathingView: View {
    @State private var isBreathing = false
    @State private var scale: CGFloat = 1.0
    @State private var breathCount = 0
    @State private var showCount = 1
    @State private var timer: Timer?
    
    var body: some View {
        VStack(spacing: 40) {
            Text("数息观")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            ZStack {
                Circle()
                    .fill(Color.blue.opacity(0.3))
                    .frame(width: 200, height: 200)
                    .scaleEffect(scale)
                    .animation(.easeInOut(duration: 4).repeatForever(autoreverses: true), value: scale)
                
                Text(isBreathing ? "呼吸" : "准备")
                    .font(.title)
                    .foregroundColor(.white)
            }
            
            Text("\(showCount)")
                .font(.system(size: 60, weight: .bold))
                .foregroundColor(.blue)
            
            Text("计数: \(breathCount)")
                .font(.title2)
            
            Button(action: {
                if isBreathing {
                    stopBreathing()
                } else {
                    startBreathing()
                }
            }) {
                Text(isBreathing ? "停止" : "开始")
                    .font(.title2)
                    .frame(width: 150, height: 50)
                    .background(isBreathing ? Color.red : Color.green)
                    .foregroundColor(.white)
                    .cornerRadius(25)
            }
        }
        .padding()
    }
    
    func startBreathing() {
        isBreathing = true
        scale = 1.3
        breathCount = 0
        showCount = 1
        
        timer = Timer.scheduledTimer(withTimeInterval: 4, repeats: true) { _ in
            breathCount += 1
            showCount += 1
            if showCount > 10 {
                showCount = 1
            }
        }
    }
    
    func stopBreathing() {
        isBreathing = false
        scale = 1.0
        timer?.invalidate()
        timer = nil
    }
}

// 冥想计时视图
struct MeditationView: View {
    @State private var selectedMinutes = 10
    @State private var timeRemaining = 600
    @State private var isRunning = false
    @State private var timer: Timer?
    let minutesOptions = [5, 10, 20, 30]
    
    var body: some View {
        VStack(spacing: 30) {
            Text("冥想")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            if !isRunning {
                Picker("时长", selection: $selectedMinutes) {
                    ForEach(minutesOptions, id: \.self) { min in
                        Text("\(min) 分钟").tag(min)
                    }
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
            }
            
            Text(formatTime(timeRemaining))
                .font(.system(size: 80, weight: .thin, design: .monospaced))
                .foregroundColor(isRunning ? .green : .primary)
            
            Button(action: {
                if isRunning {
                    stopMeditation()
                } else {
                    startMeditation()
                }
            }) {
                Text(isRunning ? "停止" : "开始")
                    .font(.title2)
                    .frame(width: 150, height: 50)
                    .background(isRunning ? Color.red : Color.green)
                    .foregroundColor(.white)
                    .cornerRadius(25)
            }
        }
        .padding()
    }
    
    func formatTime(_ seconds: Int) -> String {
        let mins = seconds / 60
        let secs = seconds % 60
        return String(format: "%02d:%02d", mins, secs)
    }
    
    func startMeditation() {
        timeRemaining = selectedMinutes * 60
        isRunning = true
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            if timeRemaining > 0 {
                timeRemaining -= 1
            } else {
                stopMeditation()
            }
        }
    }
    
    func stopMeditation() {
        isRunning = false
        timer?.invalidate()
        timer = nil
    }
}

// 功课打卡视图
struct PracticeView: View {
    @AppStorage("practiceData") private var practiceData: String = "{}"
    @State private var practices: [PracticeItem] = [
        PracticeItem(id: 1, title: "不杀生", desc: "尊重生命，心怀慈悲"),
        PracticeItem(id: 2, title: "不偷盗", desc: "诚实守信，不取非分"),
        PracticeItem(id: 3, title: "不邪淫", desc: "洁身自好，尊重他人"),
        PracticeItem(id: 4, title: "不妄语", desc: "言语真实，不欺不诈"),
        PracticeItem(id: 5, title: "不饮酒", desc: "保持清醒，远离沉迷")
    ]
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("今日功课")) {
                    ForEach($practices) { $practice in
                        PracticeRow(practice: $practice)
                    }
                }
            }
            .navigationTitle("功课")
        }
    }
}

struct PracticeItem: Identifiable {
    let id: Int
    let title: String
    let desc: String
    var completed: Bool = false
}

struct PracticeRow: View {
    @Binding var practice: PracticeItem
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(practice.title)
                    .font(.headline)
                Text(practice.desc)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            Spacer()
            Image(systemName: practice.completed ? "checkmark.circle.fill" : "circle")
                .foregroundColor(practice.completed ? .green : .gray)
                .font(.title2)
        }
        .contentShape(Rectangle())
        .onTapGesture {
            practice.completed.toggle()
        }
    }
}

// 统计视图
struct StatsView: View {
    @AppStorage("totalMinutes") private var totalMinutes: Int = 0
    @AppStorage("totalDays") private var totalDays: Int = 0
    @AppStorage("streakDays") private var streakDays: Int = 0
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("修习统计")) {
                    StatRow(title: "累计修习", value: "\(totalMinutes) 分钟", icon: "clock")
                    StatRow(title: "修习天数", value: "\(totalDays) 天", icon: "calendar")
                    StatRow(title: "连续天数", value: "\(streakDays) 天", icon: "flame.fill")
                }
            }
            .navigationTitle("统计")
        }
    }
}

struct StatRow: View {
    let title: String
    let value: String
    let icon: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .