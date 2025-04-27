from bs4 import BeautifulSoup
import re

# 假设html_content是HTML文件内容
with open('trans.html', 'r', encoding='utf-8') as file:
    html_content = file.read()

soup = BeautifulSoup(html_content, 'html.parser')

# 查找所有交易记录
transactions = soup.find_all('div', class_='trans-list-tabulate-td')

records = []

for trans in transactions:
    # 提取类型和状态
    type_div = trans.find('div', class_='trans-list-tabulate-td-box-col-text')
    status_div = trans.find('div', class_='trans-list-tabulate-td-box-col-label')

    trans_type = type_div.text.strip() if type_div else "未知"
    status = status_div.text.strip() if status_div else "未知"

    # 提取名称和时间
    name_div = trans.find('div', class_='trans-list-tabulate-td-box-col-righttext')
    time_div = trans.find('div', class_='trans-list-tabulate-td-box-col-datetext')

    name = name_div.text.strip() if name_div else "未知"
    time = time_div.text.strip() if time_div else "未知"

    # 提取克重和金价
    weight_divs = trans.find_all('div', class_='trans-list-tabulate-td-box-col-amount')
    price_divs = trans.find_all('div', class_='trans-list-tabulate-td-box-col-datetext')

    weight = weight_divs[0].text.strip() if len(weight_divs) > 0 else "0克"
    price = price_divs[1].text.strip() if len(price_divs) > 1 else "0元"

    # 提取金额和手续费
    amount = weight_divs[1].text.strip() if len(weight_divs) > 1 else "0元"
    fee = price_divs[2].text.strip() if len(price_divs) > 2 else "0元"

    # 清理数据
    weight = re.sub(r'[^\d.]', '', weight)
    price = re.sub(r'[^\d.]', '', price)
    amount = re.sub(r'[^\d.]', '', amount)
    fee = re.sub(r'[^\d.]', '', fee)

    # 转换为数值
    try:
        weight = float(weight) if weight else 0
    except:
        weight = 0

    try:
        price = float(price) if price else 0
    except:
        price = 0

    try:
        amount = float(amount) if amount else 0
    except:
        amount = 0

    try:
        fee = float(fee) if fee else 0
    except:
        fee = 0

    record = {
        '类型': trans_type,
        '状态': status,
        '名称': name,
        '时间': time,
        '克重(克)': weight,
        '单价(元/克)': price,
        '总金额(元)': amount,
        '手续费(元)': fee
    }
    records.append(record)

# 打印结果
for idx, record in enumerate(records, 1):
    print(f"记录 {idx}:")
    print(f"  类型: {record['类型']}")
    print(f"  状态: {record['状态']}")
    print(f"  名称: {record['名称']}")
    print(f"  时间: {record['时间']}")
    print(f"  克重: {record['克重(克)']}克")
    print(f"  单价: {record['单价(元/克)']}元/克")
    print(f"  总金额: {record['总金额(元)']}元")
    print(f"  手续费: {record['手续费(元)']}元")
    print("-" * 50)

# 可选：将结果保存为CSV文件
import csv

with open('tans.csv', 'w', newline='', encoding='utf-8-sig') as csvfile:
    fieldnames = ['类型', '状态', '名称', '时间', '克重(克)', '单价(元/克)', '总金额(元)', '手续费(元)']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()
    for record in records:
        writer.writerow(record)


print("解析完成，结果已保存到'交易记录.csv'")