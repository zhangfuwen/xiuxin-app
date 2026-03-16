// swift-tools-version:5.7
import PackageDescription

let package = Package(
    name: "XiuxinNative",
    platforms: [.iOS(.v15)],
    products: [
        .executable(name: "XiuxinNative", targets: ["XiuxinNative"])
    ],
    targets: [
        .executableTarget(
            name: "XiuxinNative",
            path: "."
        )
    ]
)
